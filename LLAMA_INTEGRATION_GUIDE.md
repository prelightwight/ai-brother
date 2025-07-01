# 🦙 Llama.cpp Integration Guide for AI Brother

## 🎯 Current Status

✅ **What's Implemented:**
- Enhanced native interface with proper model lifecycle management
- Improved Kotlin wrapper with async operations and error handling
- Better UI with loading states and model status
- Proper file handling and caching
- Enhanced mock responses for better testing

⚠️ **What's Still Needed:**
- Actual llama.cpp library integration
- Real GGUF model loading and inference
- Memory optimization for mobile devices
- GPU acceleration (optional)

## 🚀 Step-by-Step Implementation

### Phase 1: Add llama.cpp to Project

#### 1.1 Download llama.cpp
```bash
# In your project root
git submodule add https://github.com/ggerganov/llama.cpp.git external/llama.cpp
cd external/llama.cpp
git checkout master  # or specific stable tag
```

#### 1.2 Update CMakeLists.txt
```cmake
cmake_minimum_required(VERSION 3.10.2)

project("aibrother")

# Add llama.cpp subdirectory
add_subdirectory(external/llama.cpp)

# Set compilation flags for Android
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++17 -O3 -DNDEBUG")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -DNDEBUG")

# Disable unnecessary llama.cpp features for mobile
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)

add_library(
        llama
        SHARED
        native-lib.cpp
)

find_library(log-lib log)

target_link_libraries(
        llama
        llama-static  # Link with llama.cpp static library
        ${log-lib}
)

target_include_directories(
        llama PRIVATE
        external/llama.cpp/include
        external/llama.cpp/src
)
```

#### 1.3 Update build.gradle.kts
```kotlin
android {
    // ... existing config ...
    
    externalNativeBuild {
        cmake {
            path = file("CMakeLists.txt")
            version = "3.22.1"
        }
    }
    
    ndk {
        abiFilters += listOf("arm64-v8a", "armeabi-v7a")
    }
}
```

### Phase 2: Replace Mock Implementation

#### 2.1 Update native-lib.cpp Header
```cpp
#include <jni.h>
#include <string>
#include <vector>
#include <memory>
#include <android/log.h>
#include <thread>

// Include actual llama.cpp headers
#include "llama.h"
#include "common.h"

#define LOG_TAG "LlamaJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global model and context
static llama_model* g_model = nullptr;
static llama_context* g_ctx = nullptr;
static std::string g_model_path = "";
static bool g_backend_initialized = false;
```

#### 2.2 Replace loadModel Function
```cpp
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_loadModel(
        JNIEnv* env,
        jobject /* this */,
        jstring modelPath
) {
    const char* path = env->GetStringUTFChars(modelPath, 0);
    LOGI("Loading model from: %s", path);
    
    // Initialize backend if not done
    if (!g_backend_initialized) {
        llama_backend_init(false);
        g_backend_initialized = true;
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
    model_params.n_gpu_layers = 0; // CPU only for now
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
    ctx_params.n_ctx = 2048; // Context size
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
    LOGI("Model loaded successfully: %s", path);
    
    env->ReleaseStringUTFChars(modelPath, path);
    return JNI_TRUE;
}
```

#### 2.3 Replace runModel Function
```cpp
extern "C"
JNIEXPORT jstring JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_runModel(
        JNIEnv* env,
        jobject /* this */,
        jstring modelPath,
        jstring prompt
) {
    const char* input = env->GetStringUTFChars(prompt, 0);
    LOGI("Running inference with prompt: %s", input);
    
    if (!g_ctx || !g_model) {
        LOGE("Model not loaded");
        env->ReleaseStringUTFChars(prompt, input);
        return env->NewStringUTF("Error: Model not loaded");
    }
    
    try {
        // Tokenize input
        std::vector<llama_token> tokens_list = ::llama_tokenize(g_ctx, input, true);
        
        const int max_tokens = 512;
        const int n_ctx = llama_n_ctx(g_ctx);
        
        // Check if prompt fits in context
        if ((int)tokens_list.size() >= n_ctx - max_tokens) {
            LOGE("Prompt too long for context window");
            env->ReleaseStringUTFChars(prompt, input);
            return env->NewStringUTF("Error: Prompt too long");
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
            env->ReleaseStringUTFChars(prompt, input);
            return env->NewStringUTF("Error: Failed to evaluate prompt");
        }
        
        std::string response = "";
        int n_cur = tokens_list.size();
        
        // Generate response tokens
        for (int i = 0; i < max_tokens; i++) {
            auto* logits = llama_get_logits_ith(g_ctx, batch.n_tokens - 1);
            
            // Simple greedy sampling
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
            
            // Convert token to string
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
        LOGI("Generated response of length: %zu", response.length());
        
        env->ReleaseStringUTFChars(prompt, input);
        return env->NewStringUTF(response.c_str());
        
    } catch (const std::exception& e) {
        LOGE("Exception during inference: %s", e.what());
        env->ReleaseStringUTFChars(prompt, input);
        return env->NewStringUTF("Error: Inference failed");
    }
}
```

#### 2.4 Update Cleanup Functions
```cpp
extern "C"
JNIEXPORT void JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_unloadModel(
        JNIEnv* env,
        jobject /* this */
) {
    LOGI("Unloading model");
    
    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (g_model) {
        llama_free_model(g_model);
        g_model = nullptr;
    }
    
    g_model_path = "";
    LOGI("Model unloaded");
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
    
    char info[512];
    snprintf(info, sizeof(info), 
        "Model: %s\nContext Size: %d\nVocab Size: %d\nStatus: Loaded\nBackend: llama.cpp",
        g_model_path.c_str(),
        llama_n_ctx(g_ctx),
        llama_n_vocab(g_model)
    );
    
    return env->NewStringUTF(info);
}
```

### Phase 3: Testing and Optimization

#### 3.1 Test with Small Models
- Start with small models (1-3B parameters)
- Test with Phi-2, TinyLlama, or similar
- Verify basic functionality before scaling up

#### 3.2 Memory Optimization
```cpp
// Add memory monitoring
static size_t get_memory_usage() {
    // Implementation depends on Android API level
    // Monitor RSS and peak memory usage
}

// Add to model loading
LOGI("Memory usage after model load: %zu MB", get_memory_usage() / 1024 / 1024);
```

#### 3.3 Performance Tuning
- Adjust context size based on available memory
- Tune thread count for device capabilities
- Consider quantization levels (Q4_0, Q5_1, etc.)

### Phase 4: Advanced Features

#### 4.1 Streaming Responses
```cpp
// Implement token-by-token streaming
// Call back to Java for each generated token
```

#### 4.2 GPU Acceleration (Optional)
```cpp
// Enable GPU layers for supported devices
model_params.n_gpu_layers = 10; // Adjust based on model and GPU memory
```

#### 4.3 Multiple Model Support
- Load multiple specialized models
- Switch contexts based on conversation type
- Implement model routing logic

## 🧪 Testing Strategy

1. **Unit Tests**: Test each native function individually
2. **Integration Tests**: Test full conversation flows
3. **Performance Tests**: Monitor memory and inference speed
4. **Device Tests**: Test on various Android devices and API levels

## 📊 Expected Performance

- **Small Models (1-3B)**: 1-5 tokens/second on mid-range devices
- **Medium Models (7B)**: 0.5-2 tokens/second on high-end devices
- **Memory Usage**: 2-8GB depending on model size and quantization

## 🚨 Important Notes

1. **Memory Management**: Always test on devices with limited RAM
2. **Battery Usage**: Monitor CPU usage and implement thermal throttling
3. **Storage**: Large models require significant storage space
4. **Permissions**: May need storage permissions for model files

## 🎯 Next Steps

1. **Immediate**: Implement basic llama.cpp integration
2. **Short-term**: Add streaming and better error handling
3. **Medium-term**: Multi-model support and optimization
4. **Long-term**: GPU acceleration and advanced features

Replace the mock implementation step by step, testing thoroughly at each phase!