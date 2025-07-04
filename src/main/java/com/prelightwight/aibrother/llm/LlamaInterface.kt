package com.prelightwight.aibrother.llm

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LlamaInterface {
    
    companion object {
        private const val TAG = "LlamaInterface"
        
        init {
            try {
                System.loadLibrary("aibrother")
                Log.i(TAG, "Native library loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native library", e)
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
    
    // Native methods
    external fun stringFromJNI(): String
    external fun initializeBackendNative(): Boolean
    external fun loadModelNative(modelPath: String): Boolean
    external fun isModelLoadedNative(): Boolean
    external fun getLoadedModelPathNative(): String
    external fun generateResponseNative(input: String): String
    external fun freeModelNative()
    external fun shutdownBackendNative()
    
    private var isInitialized = false
    
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Initializing AI backend...")
            val result = initializeBackendNative()
            if (result) {
                isInitialized = true
                Log.i(TAG, "Backend initialized successfully")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Exception during initialization", e)
            false
        }
    }
    
    suspend fun loadModel(modelPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized) {
                initialize()
            }
            
            val file = File(modelPath)
            if (!file.exists()) {
                Log.e(TAG, "Model file does not exist: $modelPath")
                return@withContext false
            }
            
            Log.i(TAG, "Loading model: $modelPath")
            val result = loadModelNative(modelPath)
            if (result) {
                Log.i(TAG, "Model loaded successfully")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Exception during model loading", e)
            false
        }
    }
    
    fun isModelLoaded(): Boolean {
        return try {
            isModelLoadedNative()
        } catch (e: Exception) {
            false
        }
    }
    
    fun getLoadedModelPath(): String {
        return try {
            getLoadedModelPathNative()
        } catch (e: Exception) {
            ""
        }
    }
    
    suspend fun generateChatResponse(userMessage: String, systemPrompt: String = ""): String = withContext(Dispatchers.IO) {
        try {
            if (!isModelLoaded()) {
                return@withContext "Please load a model first from the Models tab!"
            }
            
            val prompt = if (systemPrompt.isNotBlank()) {
                "System: $systemPrompt\n\nUser: $userMessage\nAssistant: "
            } else {
                "User: $userMessage\nAssistant: "
            }
            
            generateResponseNative(prompt)
        } catch (e: Exception) {
            "Error generating response: ${e.message}"
        }
    }
    
    suspend fun freeModel() = withContext(Dispatchers.IO) {
        try {
            freeModelNative()
        } catch (e: Exception) {
            Log.e(TAG, "Error freeing model", e)
        }
    }
    
    fun testConnection(): String {
        return try {
            stringFromJNI()
        } catch (e: Exception) {
            "Connection failed: ${e.message}"
        }
    }
}