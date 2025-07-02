#include <jni.h>
#include <string>
#include <vector>
#include <memory>
#include <android/log.h>
#include <fstream>
#include <thread>
#include <chrono>
#include <cstring>

// Include actual llama.cpp headers
#include "llama.h"
#include "common.h"

#define LOG_TAG "LlamaJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

// Global model and context
static llama_model* g_model = nullptr;
static llama_context* g_ctx = nullptr;
static std::string g_model_path = "";
static bool g_backend_initialized = false;

// Helper function to check if file exists and get size
bool validate_model_file(const std::string& path, size_t& file_size) {
    std::ifstream file(path, std::ios::binary | std::ios::ate);
    if (!file.is_open()) {
        LOGE("Cannot open file: %s", path.c_str());
        return false;
    }
    
    file_size = file.tellg();
    file.close();
    
    if (file_size < 1024) { // At least 1KB
        LOGE("File too small: %zu bytes", file_size);
        return false;
    }
    
    // Check file extension
    if (path.find(".gguf") == std::string::npos && path.find(".bin") == std::string::npos) {
        LOGW("Warning: File doesn't appear to be a GGUF model: %s", path.c_str());
    }
    
    LOGI("File validation passed: %s (%zu MB)", path.c_str(), file_size / 1024 / 1024);
    return true;
}



extern "C"
JNIEXPORT jboolean JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_loadModel(
        JNIEnv* env,
        jobject /* this */,
        jstring modelPath
) {
    const char* path = env->GetStringUTFChars(modelPath, 0);
    LOGI("Loading model from: %s", path);
    
    if (!path || strlen(path) == 0) {
        LOGE("Invalid model path provided");
        env->ReleaseStringUTFChars(modelPath, path);
        return JNI_FALSE;
    }
    
    // Validate model file
    size_t file_size = 0;
    if (!validate_model_file(std::string(path), file_size)) {
        env->ReleaseStringUTFChars(modelPath, path);
        return JNI_FALSE;
    }
    
    try {
        // Initialize backend if not done
        if (!g_backend_initialized) {
            llama_backend_init(false);
            g_backend_initialized = true;
            LOGI("llama.cpp backend initialized");
        }
        
        // Unload existing model if any
        if (g_ctx) {
            llama_free(g_ctx);
            g_ctx = nullptr;
        }
        if (g_model) {
            llama_free_model(g_model);
            g_model = nullptr;
        }
        
        // Load model
        llama_model_params model_params = llama_model_default_params();
        model_params.n_gpu_layers = 0; // CPU only for Android
        model_params.use_mmap = true;
        model_params.use_mlock = false;
        
        g_model = llama_load_model_from_file(path, model_params);
        if (!g_model) {
            LOGE("Failed to load model from %s", path);
            env->ReleaseStringUTFChars(modelPath, path);
            return JNI_FALSE;
        }
        
        // Create context
        llama_context_params ctx_params = llama_context_default_params();
        ctx_params.seed = -1;
        ctx_params.n_ctx = 2048; // Context size - adjust based on available memory
        ctx_params.n_threads = std::min(4, (int)std::thread::hardware_concurrency());
        ctx_params.n_threads_batch = ctx_params.n_threads;
        
        g_ctx = llama_new_context_with_model(g_model, ctx_params);
        if (!g_ctx) {
            LOGE("Failed to create context");
            llama_free_model(g_model);
            g_model = nullptr;
            env->ReleaseStringUTFChars(modelPath, path);
            return JNI_FALSE;
        }
        
        g_model_path = std::string(path);
        LOGI("Model loaded successfully: %s (%zu MB)", path, file_size / 1024 / 1024);
        LOGI("Context size: %d, Vocab size: %d", 
             llama_n_ctx(g_ctx), llama_n_vocab(g_model));
        
    } catch (const std::exception& e) {
        LOGE("Exception during model loading: %s", e.what());
        env->ReleaseStringUTFChars(modelPath, path);
        return JNI_FALSE;
    }
    
    env->ReleaseStringUTFChars(modelPath, path);
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_runModel(
        JNIEnv* env,
        jobject /* this */,
        jstring modelPath,
        jstring prompt
) {
    const char* path = env->GetStringUTFChars(modelPath, 0);
    const char* input = env->GetStringUTFChars(prompt, 0);
    
    if (!input || strlen(input) == 0) {
        LOGE("Empty prompt provided");
        env->ReleaseStringUTFChars(modelPath, path);
        env->ReleaseStringUTFChars(prompt, input);
        return env->NewStringUTF("Error: Empty prompt provided");
    }
    
    LOGI("Running inference with prompt length: %zu", strlen(input));
    
    // Check if model is loaded
    if (!g_model || !g_ctx) {
        LOGE("Model not loaded");
        env->ReleaseStringUTFChars(modelPath, path);
        env->ReleaseStringUTFChars(prompt, input);
        return env->NewStringUTF("Error: Model not loaded");
    }
    
    // Check prompt length
    size_t prompt_len = strlen(input);
    if (prompt_len > 8000) {
        LOGE("Prompt too long: %zu characters", prompt_len);
        env->ReleaseStringUTFChars(modelPath, path);
        env->ReleaseStringUTFChars(prompt, input);
        return env->NewStringUTF("Error: Prompt too long (maximum 8000 characters)");
    }
    
    std::string response;
    auto start_time = std::chrono::high_resolution_clock::now();
    
    try {
        // Tokenize input
        std::vector<llama_token> tokens_list = ::llama_tokenize(g_ctx, input, true);
        
        const int max_tokens = 512;
        const int n_ctx = llama_n_ctx(g_ctx);
        
        // Check if prompt fits in context
        if ((int)tokens_list.size() >= n_ctx - max_tokens) {
            LOGE("Prompt too long for context window");
            env->ReleaseStringUTFChars(modelPath, path);
            env->ReleaseStringUTFChars(prompt, input);
            return env->NewStringUTF("Error: Prompt too long for context window");
        }
        
        // Clear the KV cache
        llama_kv_cache_clear(g_ctx);
        
        // Evaluate the prompt
        llama_batch batch = llama_batch_init(512, 0, 1);
        
        for (size_t i = 0; i < tokens_list.size(); i++) {
            llama_batch_add(batch, tokens_list[i], i, {0}, false);
        }
        batch.logits[batch.n_tokens - 1] = true;
        
        if (llama_decode(g_ctx, batch) != 0) {
            LOGE("Failed to evaluate prompt");
            llama_batch_free(batch);
            env->ReleaseStringUTFChars(modelPath, path);
            env->ReleaseStringUTFChars(prompt, input);
            return env->NewStringUTF("Error: Failed to evaluate prompt");
        }
        
        response = "";
        int n_cur = tokens_list.size();
        
        // Generate response tokens
        for (int i = 0; i < max_tokens; i++) {
            auto* logits = llama_get_logits_ith(g_ctx, batch.n_tokens - 1);
            
            // Simple greedy sampling for now
            auto candidates = std::vector<llama_token_data>();
            candidates.reserve(llama_n_vocab(g_model));
            for (llama_token token_id = 0; token_id < llama_n_vocab(g_model); token_id++) {
                candidates.emplace_back(llama_token_data{token_id, logits[token_id], 0.0f});
            }
            
            llama_token_data_array candidates_p = { candidates.data(), candidates.size(), false };
            llama_token next_token = llama_sample_token_greedy(g_ctx, &candidates_p);
            
            // Check for EOS
            if (next_token == llama_token_eos(g_model)) {
                break;
            }
            
            // Convert token to string and add to response
            std::string token_str = llama_token_to_piece(g_ctx, next_token);
            response += token_str;
            
            // Prepare next iteration
            llama_batch_clear(batch);
            llama_batch_add(batch, next_token, n_cur, {0}, true);
            
            if (llama_decode(g_ctx, batch) != 0) {
                LOGE("Failed to evaluate token");
                break;
            }
            
            n_cur++;
        }
        
        llama_batch_free(batch);
        
    } catch (const std::exception& e) {
        LOGE("Exception during inference: %s", e.what());
        env->ReleaseStringUTFChars(modelPath, path);
        env->ReleaseStringUTFChars(prompt, input);
        return env->NewStringUTF("Error: Inference failed due to internal error");
    }
    
    auto end_time = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end_time - start_time);
    
    LOGI("Generated response of length: %zu in %lld ms", response.length(), duration.count());
    
    env->ReleaseStringUTFChars(modelPath, path);
    env->ReleaseStringUTFChars(prompt, input);
    return env->NewStringUTF(response.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_unloadModel(
        JNIEnv* env,
        jobject /* this */
) {
    LOGI("Unloading model");
    
    try {
        if (g_ctx) {
            llama_free(g_ctx);
            g_ctx = nullptr;
        }
        if (g_model) {
            llama_free_model(g_model);
            g_model = nullptr;
        }
        
        g_model_path = "";
        LOGI("Model unloaded successfully");
        
    } catch (const std::exception& e) {
        LOGE("Exception during model unloading: %s", e.what());
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_isModelLoaded(
        JNIEnv* env,
        jobject /* this */
) {
    bool loaded = (g_model != nullptr && g_ctx != nullptr);
    
    LOGI("Model loaded check: %s", loaded ? "true" : "false");
    return loaded ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_getModelInfo(
        JNIEnv* env,
        jobject /* this */
) {
    if (!g_model || !g_ctx) {
        return env->NewStringUTF("No model loaded");
    }
    
    try {
        char info[512];
        snprintf(info, sizeof(info), 
            "Model: %s\n"
            "Context Size: %d\n"
            "Vocab Size: %d\n"
            "Status: Loaded\n"
            "Backend: llama.cpp\n"
            "Threads: %d",
            g_model_path.c_str(),
            llama_n_ctx(g_ctx),
            llama_n_vocab(g_model),
            std::min(4, (int)std::thread::hardware_concurrency())
        );
        
        return env->NewStringUTF(info);
        
    } catch (const std::exception& e) {
        LOGE("Exception getting model info: %s", e.what());
        return env->NewStringUTF("Error retrieving model information");
    }
}
