package com.prelightwight.aibrother.llm

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LlamaInterface {
    
    companion object {
        private const val TAG = "LlamaInterface"
        
        private var nativeLibraryLoaded = false
        
        init {
            try {
                System.loadLibrary("aibrother")
                nativeLibraryLoaded = true
                Log.i(TAG, "Native library loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "Native library not available, using mock implementation")
                nativeLibraryLoaded = false
            }
        }
        
        @Volatile
        private var INSTANCE: LlamaInterface? = null
        
        fun getInstance(): LlamaInterface {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LlamaInterface().also { INSTANCE = it }
            }
        }
    }
    
    // Native methods - only used if library is loaded
    external fun stringFromJNI(): String
    external fun initializeBackendNative(): Boolean
    external fun isModelLoadedNative(): Boolean
    external fun getLoadedModelPathNative(): String
    external fun generateResponseNative(input: String): String
    external fun freeModelNative()
    external fun shutdownBackendNative()
    external fun loadModelNative(modelPath: String): Boolean
    
    // State management for both native and mock modes
    private var backendInitialized = false
    private var mockModelLoaded = false
    private var mockModelPath = ""
    private val mockResponses = listOf(
        "I'm running in simulation mode while we set up the native AI library.",
        "This is a mock response - the real AI model will provide much better answers once loaded!",
        "Mock AI response: I understand your question and would provide a detailed answer with a real model.",
        "Simulated response: The AI system is working, but using fallback responses until native integration is complete."
    )
    
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (nativeLibraryLoaded) {
                Log.i(TAG, "Initializing native AI backend...")
                val result = initializeBackendNative()
                if (result) {
                    Log.i(TAG, "Native backend initialized successfully")
                    backendInitialized = true
                } else {
                    Log.w(TAG, "Native backend initialization failed, falling back to mock")
                    backendInitialized = true // Still allow mock mode
                }
                backendInitialized
            } else {
                Log.i(TAG, "Initializing mock AI backend...")
                backendInitialized = true
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during initialization, using mock mode", e)
            backendInitialized = true
            true
        }
    }
    
    suspend fun loadModel(modelPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(modelPath)
            if (!file.exists()) {
                Log.e(TAG, "Model file does not exist: $modelPath")
                return@withContext false
            }
            
            Log.i(TAG, "Loading model: $modelPath (native=$nativeLibraryLoaded, initialized=$backendInitialized)")
            
            if (nativeLibraryLoaded && backendInitialized) {
                Log.i(TAG, "Attempting native model loading...")
                try {
                    val result = loadModelNative(modelPath)
                    if (result) {
                        Log.i(TAG, "Native model loaded successfully")
                        // Also update mock state for consistency
                        mockModelPath = modelPath
                        mockModelLoaded = true
                        return@withContext true
                    } else {
                        Log.w(TAG, "Native model loading failed, falling back to mock")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during native model loading, falling back to mock", e)
                }
            }
            
            // Always fall back to mock mode if native fails or isn't available
            Log.i(TAG, "Loading model in mock mode: $modelPath")
            mockModelPath = modelPath
            mockModelLoaded = true
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during model loading", e)
            false
        }
    }
    
    fun isModelLoaded(): Boolean {
        return try {
            // Check native first if available, otherwise check mock
            if (nativeLibraryLoaded && backendInitialized) {
                try {
                    val nativeLoaded = isModelLoadedNative()
                    Log.d(TAG, "Native model loaded check: $nativeLoaded")
                    if (nativeLoaded) {
                        return true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Exception checking native model status, checking mock", e)
                }
            }
            
            // Check mock state
            Log.d(TAG, "Mock model loaded check: $mockModelLoaded")
            mockModelLoaded
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception in isModelLoaded", e)
            mockModelLoaded
        }
    }
    
    fun getLoadedModelPath(): String {
        return try {
            if (nativeLibraryLoaded && backendInitialized) {
                try {
                    val nativePath = getLoadedModelPathNative()
                    if (nativePath.isNotEmpty()) {
                        return nativePath
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Exception getting native model path, using mock", e)
                }
            }
            mockModelPath
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getLoadedModelPath", e)
            mockModelPath
        }
    }
    
    suspend fun generateChatResponse(userMessage: String, systemPrompt: String = ""): String = withContext(Dispatchers.IO) {
        try {
            if (!isModelLoaded()) {
                return@withContext "Please load a model first from the Models tab!"
            }
            
            Log.d(TAG, "Generating response (native=$nativeLibraryLoaded, initialized=$backendInitialized)")
            
            if (nativeLibraryLoaded && backendInitialized) {
                try {
                    val prompt = if (systemPrompt.isNotBlank()) {
                        "System: $systemPrompt\n\nUser: $userMessage\nAssistant: "
                    } else {
                        "User: $userMessage\nAssistant: "
                    }
                    val response = generateResponseNative(prompt)
                    if (response.isNotEmpty()) {
                        return@withContext response
                    } else {
                        Log.w(TAG, "Native response was empty, falling back to mock")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Exception during native response generation, falling back to mock", e)
                }
            }
            
            // Mock response
            val modelName = when {
                mockModelPath.contains("tinyllama", ignoreCase = true) -> "TinyLlama"
                mockModelPath.contains("phi", ignoreCase = true) -> "Phi-3"
                mockModelPath.contains("gemma", ignoreCase = true) -> "Gemma 2"
                mockModelPath.contains("llama", ignoreCase = true) -> "Llama 3.2"
                mockModelPath.contains("qwen", ignoreCase = true) -> "Qwen2"
                else -> "AI Model"
            }
            
            val baseResponse = mockResponses.random()
            "$baseResponse\n\n(Currently using $modelName in simulation mode - real AI responses coming soon!)"
            
        } catch (e: Exception) {
            "Error generating response: ${e.message}"
        }
    }
    
    suspend fun freeModel() = withContext(Dispatchers.IO) {
        try {
            if (nativeLibraryLoaded && backendInitialized) {
                try {
                    freeModelNative()
                } catch (e: Exception) {
                    Log.w(TAG, "Exception freeing native model", e)
                }
            }
            // Always clear mock state
            mockModelLoaded = false
            mockModelPath = ""
        } catch (e: Exception) {
            Log.e(TAG, "Error freeing model", e)
            mockModelLoaded = false
            mockModelPath = ""
        }
    }
    
    fun testConnection(): String {
        return try {
            if (nativeLibraryLoaded) {
                stringFromJNI()
            } else {
                "AI Brother Mock Library v1.4.0 - Native library will be enabled soon!"
            }
        } catch (e: Exception) {
            "Mock connection active: ${e.message}"
        }
    }
}