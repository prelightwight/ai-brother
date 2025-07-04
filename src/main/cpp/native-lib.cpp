#include <jni.h>
#include <string>
#include <map>
#include <vector>
#include <random>
#include <ctime>
#include <thread>
#include <chrono>
#include <algorithm>

// Mock AI state management
static bool backendInitialized = false;
static bool modelLoaded = false;
static std::string loadedModelPath = "";

// Mock conversation context
static std::vector<std::string> conversationHistory;
static std::map<std::string, std::vector<std::string>> responseTemplates;

// Initialize response templates
void initializeResponseTemplates() {
    static bool initialized = false;
    if (initialized) return;
    
    responseTemplates["greeting"] = {
        "Hello! I'm running locally on your device. How can I help you today?",
        "Hi there! Your local AI assistant is ready to chat privately and securely!",
        "Greetings! I'm here to help with whatever you need."
    };
    
    responseTemplates["general"] = {
        "That's an interesting topic! I'd love to explore this further with you.",
        "I understand what you're asking. Let me provide you with a thoughtful response.",
        "Your question is quite thought-provoking. Here's what I think about that...",
        "From my perspective as a local AI assistant, I can help you understand this better."
    };
    
    responseTemplates["tech"] = {
        "That's a great technical question! As an AI model running directly on your Android device, I can explain this concept.",
        "From a technical standpoint, I can break this down for you step by step.",
        "That's an excellent question about technology! Let me share my understanding."
    };
    
    responseTemplates["help"] = {
        "I'm here to help! As your local AI assistant, I can provide information, answer questions, and have conversations.",
        "I'd be happy to assist you! What specific information or help do you need?",
        "Of course! That's what I'm here for. Let me help you with that."
    };
    
    initialized = true;
}

// Generate context-aware response
std::string generateContextualResponse(const std::string& input) {
    initializeResponseTemplates();
    
    std::string lowerInput = input;
    std::transform(lowerInput.begin(), lowerInput.end(), lowerInput.begin(), ::tolower);
    
    // Determine response category
    std::string category = "general";
    if (lowerInput.find("hello") != std::string::npos || 
        lowerInput.find("hi") != std::string::npos ||
        lowerInput.find("hey") != std::string::npos) {
        category = "greeting";
    } else if (lowerInput.find("help") != std::string::npos ||
               lowerInput.find("assist") != std::string::npos) {
        category = "help";
    } else if (lowerInput.find("technical") != std::string::npos ||
               lowerInput.find("code") != std::string::npos ||
               lowerInput.find("programming") != std::string::npos ||
               lowerInput.find("computer") != std::string::npos) {
        category = "tech";
    }
    
    // Get random response from category
    auto& responses = responseTemplates[category];
    static std::mt19937 rng(std::time(nullptr));
    std::uniform_int_distribution<int> dist(0, responses.size() - 1);
    
    std::string baseResponse = responses[dist(rng)];
    
    // Add some contextual information based on loaded model
    if (!loadedModelPath.empty()) {
        if (loadedModelPath.find("tinyllama") != std::string::npos) {
            baseResponse += "\n\n(Powered by TinyLlama - fast and efficient!)";
        } else if (loadedModelPath.find("phi") != std::string::npos) {
            baseResponse += "\n\n(Powered by Phi-3 - Microsoft's advanced small model!)";
        } else if (loadedModelPath.find("gemma") != std::string::npos) {
            baseResponse += "\n\n(Powered by Gemma 2 - Google's latest model!)";
        } else if (loadedModelPath.find("llama") != std::string::npos) {
            baseResponse += "\n\n(Powered by Llama 3.2 - Meta's compact model!)";
        } else if (loadedModelPath.find("qwen") != std::string::npos) {
            baseResponse += "\n\n(Powered by Qwen2 - excellent multilingual capabilities!)";
        }
    }
    
    // Add conversation context if available
    if (conversationHistory.size() > 1) {
        baseResponse += "\n\nI remember we were discussing some interesting topics earlier.";
    }
    
    // Store in conversation history (keep last 10 messages)
    conversationHistory.push_back(input);
    if (conversationHistory.size() > 10) {
        conversationHistory.erase(conversationHistory.begin());
    }
    
    return baseResponse;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_stringFromJNI(JNIEnv *env, jobject /* this */) {
    std::string hello = "AI Brother Native Library v1.0 - Ready for AI Models";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_initializeBackendNative(JNIEnv *env, jobject /* this */) {
    backendInitialized = true;
    initializeResponseTemplates();
    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_loadModelNative(JNIEnv *env, jobject /* this */, jstring modelPath) {
    if (!backendInitialized) return JNI_FALSE;
    
    const char *path = env->GetStringUTFChars(modelPath, 0);
    loadedModelPath = std::string(path);
    env->ReleaseStringUTFChars(modelPath, path);
    
    // Simulate model loading delay
    std::this_thread::sleep_for(std::chrono::milliseconds(1000));
    
    modelLoaded = true;
    conversationHistory.clear(); // Reset conversation when new model loads
    
    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_isModelLoadedNative(JNIEnv *env, jobject /* this */) {
    return modelLoaded ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_getLoadedModelPathNative(JNIEnv *env, jobject /* this */) {
    return env->NewStringUTF(loadedModelPath.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_generateResponseNative(JNIEnv *env, jobject /* this */, jstring input) {
    if (!modelLoaded) {
        return env->NewStringUTF("Model not loaded. Please load a model first.");
    }
    
    const char *inputStr = env->GetStringUTFChars(input, 0);
    std::string userInput(inputStr);
    env->ReleaseStringUTFChars(input, inputStr);
    
    // Simulate processing time (realistic for local AI)
    std::this_thread::sleep_for(std::chrono::milliseconds(500 + (rand() % 1000)));
    
    std::string response = generateContextualResponse(userInput);
    
    return env->NewStringUTF(response.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_freeModelNative(JNIEnv *env, jobject /* this */) {
    modelLoaded = false;
    loadedModelPath = "";
    conversationHistory.clear();
}

extern "C" JNIEXPORT void JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_shutdownBackendNative(JNIEnv *env, jobject /* this */) {
    backendInitialized = false;
    modelLoaded = false;
    loadedModelPath = "";
    conversationHistory.clear();
}
