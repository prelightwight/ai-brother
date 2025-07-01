package com.prelightwight.aibrother.llm

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object LlamaRunner {
    private const val TAG = "LlamaRunner"
    
    private var modelUri: Uri? = null
    private var appContext: Context? = null
    private var currentModelPath: String? = null
    private var isModelLoading = false

    fun init(context: Context) {
        appContext = context.applicationContext
        Log.i(TAG, "LlamaRunner initialized")
    }

    fun setModelUri(uri: Uri) {
        // Unload current model if switching
        if (currentModelPath != null && LlamaInterface.isModelLoaded()) {
            Log.i(TAG, "Unloading previous model")
            LlamaInterface.unloadModel()
            currentModelPath = null
        }
        
        modelUri = uri
        Log.i(TAG, "Model URI set: $uri")
    }

    suspend fun loadModel(): String = withContext(Dispatchers.IO) {
        val context = appContext ?: return@withContext "Error: No context set"
        val uri = modelUri ?: return@withContext "Error: No model selected"

        if (isModelLoading) {
            return@withContext "Error: Model is already loading"
        }

        try {
            isModelLoading = true
            Log.i(TAG, "Starting model loading process")

            // Copy model to cache if not already there
            val modelFile = getOrCopyModelToCache(context, uri)
                ?: return@withContext "Error: Failed to access model file"

            // Load model using native interface
            val success = LlamaInterface.loadModel(modelFile.absolutePath)
            if (success) {
                currentModelPath = modelFile.absolutePath
                Log.i(TAG, "Model loaded successfully: ${modelFile.name}")
                "Model loaded successfully: ${modelFile.name}"
            } else {
                Log.e(TAG, "Failed to load model via native interface")
                "Error: Failed to load model"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during model loading", e)
            "Error: ${e.message}"
        } finally {
            isModelLoading = false
        }
    }

    suspend fun infer(prompt: String): String = withContext(Dispatchers.IO) {
        val context = appContext ?: return@withContext "Error: No context set"
        val uri = modelUri ?: return@withContext "Error: No model selected"

        try {
            // Ensure model is loaded
            if (!LlamaInterface.isModelLoaded()) {
                Log.w(TAG, "Model not loaded, attempting to load")
                val loadResult = loadModel()
                if (loadResult.startsWith("Error:")) {
                    return@withContext loadResult
                }
            }

            val modelPath = currentModelPath ?: return@withContext "Error: Model path not available"
            
            Log.i(TAG, "Running inference with prompt length: ${prompt.length}")
            val startTime = System.currentTimeMillis()
            
            val response = LlamaInterface.runModel(modelPath, prompt)
            
            val endTime = System.currentTimeMillis()
            Log.i(TAG, "Inference completed in ${endTime - startTime}ms")
            
            response
        } catch (e: Exception) {
            Log.e(TAG, "Exception during inference", e)
            "Error: ${e.message}"
        }
    }

    fun isModelLoaded(): Boolean {
        return LlamaInterface.isModelLoaded()
    }

    fun getModelInfo(): String {
        return if (isModelLoaded()) {
            LlamaInterface.getModelInfo()
        } else {
            "No model loaded"
        }
    }

    fun unloadModel() {
        if (LlamaInterface.isModelLoaded()) {
            Log.i(TAG, "Unloading model")
            LlamaInterface.unloadModel()
            currentModelPath = null
        }
    }

    private fun getOrCopyModelToCache(context: Context, uri: Uri): File? {
        return try {
            val fileName = getFileNameFromUri(context, uri) ?: "model.gguf"
            val cachedFile = File(context.cacheDir, fileName)
            
            // Check if file already exists and is the same
            if (cachedFile.exists()) {
                Log.i(TAG, "Using cached model file: ${cachedFile.name}")
                return cachedFile
            }
            
            Log.i(TAG, "Copying model file to cache: $fileName")
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null

            FileOutputStream(cachedFile).use { output ->
                inputStream.copyTo(output)
            }

            Log.i(TAG, "Model file copied successfully. Size: ${cachedFile.length()} bytes")
            cachedFile
        } catch (e: Exception) {
            Log.e(TAG, "Error copying model to cache", e)
            null
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }

    // Clean up when app is closing
    fun cleanup() {
        Log.i(TAG, "Cleaning up LlamaRunner")
        unloadModel()
        currentModelPath = null
        modelUri = null
    }
}
