#include <jni.h>
#include <string>
#include <vector>
#include <memory>
#include <thread>
#include <mutex>
#include <android/log.h>

// Real llama.cpp includes
#include "llama.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "AIBrother", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "AIBrother", __VA_ARGS__)

// Global state for llama.cpp
static std::mutex g_mutex;
static llama_model* g_model = nullptr;
static llama_context* g_ctx = nullptr;
static std::string g_model_path = "";
static bool g_backend_initialized = false;

// Model parameters
struct ModelParams {
    int32_t n_ctx = 2048;           // Context length
    int32_t n_batch = 512;          // Batch size
    int32_t n_threads = 4;          // Number of threads
    float temp = 0.7f;              // Temperature
    int32_t top_k = 40;             // Top-k sampling
    float top_p = 0.9f;             // Top-p sampling
    float repeat_penalty = 1.1f;    // Repetition penalty
};

static ModelParams g_params;

// Initialize llama.cpp backend
bool initialize_llama_backend() {
    if (g_backend_initialized) return true;
    
    try {
        // Initialize llama backend
        llama_backend_init();
        
        // Set number of threads based on CPU cores
        g_params.n_threads = std::min(4, (int)std::thread::hardware_concurrency());
        
        g_backend_initialized = true;
        LOGI("✅ Llama.cpp backend initialized successfully");
        return true;
    } catch (const std::exception& e) {
        LOGE("❌ Failed to initialize llama backend: %s", e.what());
        return false;
    }
}

// Load model from file path
bool load_model(const std::string& model_path) {
    std::lock_guard<std::mutex> lock(g_mutex);
    
    if (!g_backend_initialized) {
        LOGE("❌ Backend not initialized");
        return false;
    }
    
    // Free existing model if loaded
    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (g_model) {
        llama_model_free(g_model);
        g_model = nullptr;
    }
    
    try {
        LOGI("🔄 Loading model from: %s", model_path.c_str());
        
        // Set up model parameters
        llama_model_params model_params = llama_model_default_params();
        model_params.n_gpu_layers = 0; // CPU only for Android
        model_params.use_mmap = true;
        model_params.use_mlock = false;
        
        // Load the model
        g_model = llama_model_load_from_file(model_path.c_str(), model_params);
        if (!g_model) {
            LOGE("❌ Failed to load model from %s", model_path.c_str());
            return false;
        }
        
        // Create context
        llama_context_params ctx_params = llama_context_default_params();
        ctx_params.n_ctx = g_params.n_ctx;
        ctx_params.n_batch = g_params.n_batch;
        ctx_params.n_threads = g_params.n_threads;
        ctx_params.n_threads_batch = g_params.n_threads;
        
        g_ctx = llama_init_from_model(g_model, ctx_params);
        if (!g_ctx) {
            LOGE("❌ Failed to create context");
            llama_model_free(g_model);
            g_model = nullptr;
            return false;
        }
        
        g_model_path = model_path;
        LOGI("✅ Model loaded successfully: %s", model_path.c_str());
        LOGI("📊 Context size: %d, Threads: %d", g_params.n_ctx, g_params.n_threads);
        
        return true;
    } catch (const std::exception& e) {
        LOGE("❌ Exception loading model: %s", e.what());
        return false;
    }
}

// Generate response using llama.cpp
std::string generate_response(const std::string& prompt) {
    std::lock_guard<std::mutex> lock(g_mutex);
    
    if (!g_model || !g_ctx) {
        return "Error: No model loaded. Please load a model first.";
    }
    
    try {
        LOGI("🤖 Generating response for prompt: %.100s...", prompt.c_str());
        
        // Get model vocab
        const llama_vocab* vocab = llama_model_get_vocab(g_model);
        if (!vocab) {
            LOGE("❌ Failed to get model vocabulary");
            return "Error: Failed to access model vocabulary.";
        }
        
        // Tokenize the prompt
        std::vector<llama_token> tokens_list;
        tokens_list.resize(prompt.length() + 1);
        
        const int n_tokens = llama_tokenize(
            vocab, 
            prompt.c_str(), 
            prompt.length(),
            tokens_list.data(), 
            tokens_list.size(), 
            true, // add_bos
            true  // special
        );
        
        if (n_tokens < 0) {
            LOGE("❌ Failed to tokenize prompt");
            return "Error: Failed to process input text.";
        }
        
        tokens_list.resize(n_tokens);
        
        // Clear the KV cache using the new memory API
        llama_memory_t memory = llama_get_memory(g_ctx);
        llama_memory_clear(memory, false);
        
        // Create batch and evaluate the prompt
        llama_batch batch = llama_batch_get_one(tokens_list.data(), n_tokens);
        if (llama_decode(g_ctx, batch)) {
            LOGE("❌ Failed to evaluate prompt");
            return "Error: Failed to process prompt.";
        }
        
        // Generate response tokens
        std::string response = "";
        const int max_tokens = 256; // Limit response length for mobile
        auto n_vocab = llama_vocab_n_tokens(vocab);
        
        for (int i = 0; i < max_tokens; i++) {
            // Get logits from the model
            auto* logits = llama_get_logits_ith(g_ctx, -1);
            
            // Apply temperature and create probability distribution
            std::vector<float> probabilities(logits, logits + n_vocab);
            for (int j = 0; j < n_vocab; j++) {
                probabilities[j] = exp(probabilities[j] / g_params.temp);
            }
            
            // Normalize probabilities
            float sum = 0.0f;
            for (float p : probabilities) sum += p;
            for (float& p : probabilities) p /= sum;
            
            // Simple sampling - pick highest probability token
            llama_token new_token_id = 0;
            float max_prob = 0.0f;
            for (int j = 0; j < n_vocab; j++) {
                if (probabilities[j] > max_prob) {
                    max_prob = probabilities[j];
                    new_token_id = j;
                }
            }
            
            // Check for end of sequence
            if (llama_vocab_is_eog(vocab, new_token_id)) {
                break;
            }
            
            // Decode token to text
            char piece[256];
            int piece_size = llama_token_to_piece(
                vocab, 
                new_token_id, 
                piece, 
                sizeof(piece), 
                0,    // lstrip
                false // special
            );
            
            if (piece_size > 0) {
                response += std::string(piece, piece_size);
            }
            
            // Evaluate the new token
            llama_batch single_batch = llama_batch_get_one(&new_token_id, 1);
            if (llama_decode(g_ctx, single_batch)) {
                LOGE("❌ Failed to evaluate token");
                break;
            }
        }
        
        LOGI("✅ Generated response: %.100s...", response.c_str());
        return response;
        
    } catch (const std::exception& e) {
        LOGE("❌ Exception during generation: %s", e.what());
        return "Error: Failed to generate response.";
    }
}

// JNI Interface Functions
extern "C" JNIEXPORT jstring JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_stringFromJNI(JNIEnv *env, jobject /* this */) {
    std::string info = "AI Brother Native Library v2.0 - Real llama.cpp Integration";
    return env->NewStringUTF(info.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_initializeBackendNative(JNIEnv *env, jobject /* this */) {
    return initialize_llama_backend() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_loadModelNative(JNIEnv *env, jobject /* this */, jstring modelPath) {
    const char *path = env->GetStringUTFChars(modelPath, 0);
    std::string model_path(path);
    env->ReleaseStringUTFChars(modelPath, path);
    
    return load_model(model_path) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_isModelLoadedNative(JNIEnv *env, jobject /* this */) {
    std::lock_guard<std::mutex> lock(g_mutex);
    return (g_model && g_ctx) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_getLoadedModelPathNative(JNIEnv *env, jobject /* this */) {
    std::lock_guard<std::mutex> lock(g_mutex);
    return env->NewStringUTF(g_model_path.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_generateResponseNative(JNIEnv *env, jobject /* this */, jstring input) {
    const char *inputStr = env->GetStringUTFChars(input, 0);
    std::string prompt(inputStr);
    env->ReleaseStringUTFChars(input, inputStr);
    
    std::string response = generate_response(prompt);
    return env->NewStringUTF(response.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_freeModelNative(JNIEnv *env, jobject /* this */) {
    std::lock_guard<std::mutex> lock(g_mutex);
    
    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (g_model) {
        llama_model_free(g_model);
        g_model = nullptr;
    }
    g_model_path.clear();
    
    LOGI("🗑️ Model freed successfully");
}

extern "C" JNIEXPORT void JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_shutdownBackendNative(JNIEnv *env, jobject /* this */) {
    std::lock_guard<std::mutex> lock(g_mutex);
    
    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (g_model) {
        llama_model_free(g_model);
        g_model = nullptr;
    }
    
    if (g_backend_initialized) {
        llama_backend_free();
        g_backend_initialized = false;
    }
    
    g_model_path.clear();
    LOGI("🔌 Llama.cpp backend shutdown complete");
}
