package com.prelightwight.aibrother.llm

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class ModelInfo(
    val name: String,
    val path: String,
    val size: Long,
    val isLoaded: Boolean,
    val contextSize: Int = 0,
    val vocabSize: Int = 0
)

data class InferenceConfig(
    val maxTokens: Int = 512,
    val temperature: Float = 0.7f,
    val topK: Int = 40,
    val topP: Float = 0.9f,
    val streamResponse: Boolean = false
)

object LlamaRunner {
    private const val TAG = "LlamaRunner"
    
    private var modelUri: Uri? = null
    private var appContext: Context? = null
    private var currentModelPath: String? = null
    private var isModelLoading = false
    private var loadingProgress = 0f
    
    // Performance monitoring
    private var lastInferenceTime = 0L
    private var averageTokensPerSecond = 0f

    fun init(context: Context) {
        appContext = context.applicationContext
        Log.i(TAG, "LlamaRunner initialized")
    }

    fun setModelUri(uri: Uri) {
        // Unload current model if switching
        if (currentModelPath != null && LlamaInterface.isModelLoaded()) {
            Log.i(TAG, "Unloading previous model")
            try {
                LlamaInterface.unloadModel()
                currentModelPath = null
            } catch (e: Exception) {
                Log.e(TAG, "Error unloading previous model", e)
            }
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
            loadingProgress = 0f
            Log.i(TAG, "Starting model loading process")

            // Validate available memory
            val runtime = Runtime.getRuntime()
            val maxMemory = runtime.maxMemory()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val availableMemory = maxMemory - usedMemory
            
            Log.i(TAG, "Memory check - Available: ${availableMemory / 1024 / 1024}MB")
            
            if (availableMemory < 100 * 1024 * 1024) { // Less than 100MB
                return@withContext "Error: Insufficient memory available (${availableMemory / 1024 / 1024}MB)"
            }

            loadingProgress = 0.2f
            
            // Copy model to cache if not already there
            val modelFile = getOrCopyModelToCache(context, uri)
                ?: return@withContext "Error: Failed to access model file"

            loadingProgress = 0.5f
            
            // Validate model file
            if (!isValidModelFile(modelFile)) {
                return@withContext "Error: Invalid model file format. Expected .gguf file."
            }

            loadingProgress = 0.7f

            // Load model using native interface
            val success = LlamaInterface.loadModel(modelFile.absolutePath)
            if (success) {
                currentModelPath = modelFile.absolutePath
                loadingProgress = 1.0f
                Log.i(TAG, "Model loaded successfully: ${modelFile.name}")
                
                // Log model info
                val info = LlamaInterface.getModelInfo()
                Log.i(TAG, "Model info: $info")
                
                "Model loaded successfully: ${modelFile.name}"
            } else {
                Log.e(TAG, "Failed to load model via native interface")
                "Error: Failed to load model. Check if the file is a valid GGUF model."
            }
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Out of memory during model loading", e)
            // Force garbage collection
            System.gc()
            "Error: Out of memory. Try using a smaller model or closing other apps."
        } catch (e: Exception) {
            Log.e(TAG, "Exception during model loading", e)
            "Error: ${e.message}"
        } finally {
            isModelLoading = false
            loadingProgress = 0f
        }
    }

    suspend fun infer(prompt: String, config: InferenceConfig = InferenceConfig()): String = withContext(Dispatchers.IO) {
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
            
            // Validate prompt
            if (prompt.isBlank()) {
                return@withContext "Error: Empty prompt provided"
            }
            
            if (prompt.length > 8000) { // Reasonable limit
                return@withContext "Error: Prompt too long (${prompt.length} characters). Maximum 8000 characters."
            }
            
            Log.i(TAG, "Running inference with prompt length: ${prompt.length}")
            val startTime = System.currentTimeMillis()
            
            val response = LlamaInterface.runModel(modelPath, prompt)
            
            val endTime = System.currentTimeMillis()
            lastInferenceTime = endTime - startTime
            
            // Calculate approximate tokens per second (rough estimate)
            val responseTokens = response.split(" ").size
            averageTokensPerSecond = if (lastInferenceTime > 0) {
                (responseTokens * 1000f) / lastInferenceTime
            } else 0f
            
            Log.i(TAG, "Inference completed in ${lastInferenceTime}ms, ~${"%.2f".format(averageTokensPerSecond)} tokens/sec")
            
            if (response.startsWith("Error:")) {
                Log.e(TAG, "Native inference error: $response")
            }
            
            response
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Out of memory during inference", e)
            System.gc()
            "Error: Out of memory during inference. Try reducing context or using a smaller model."
        } catch (e: Exception) {
            Log.e(TAG, "Exception during inference", e)
            "Error: ${e.message}"
        }
    }

    // Future: Streaming inference support
    suspend fun inferStream(prompt: String, config: InferenceConfig = InferenceConfig()): Flow<String> = flow {
        // For now, emit the full response at once
        // TODO: Implement actual streaming when native layer supports it
        val response = infer(prompt, config)
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun isModelLoaded(): Boolean {
        return try {
            LlamaInterface.isModelLoaded()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking model status", e)
            false
        }
    }

    fun getModelInfo(): ModelInfo? {
        return try {
            if (!isModelLoaded()) return null
            
            val info = LlamaInterface.getModelInfo()
            val modelFile = currentModelPath?.let { File(it) }
            
            ModelInfo(
                name = modelFile?.name ?: "Unknown",
                path = currentModelPath ?: "",
                size = modelFile?.length() ?: 0,
                isLoaded = true,
                contextSize = extractContextSize(info),
                vocabSize = extractVocabSize(info)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting model info", e)
            null
        }
    }

    fun getPerformanceStats(): Map<String, Any> {
        return mapOf(
            "lastInferenceTimeMs" to lastInferenceTime,
            "averageTokensPerSecond" to averageTokensPerSecond,
            "isModelLoaded" to isModelLoaded(),
            "loadingProgress" to loadingProgress
        )
    }

    fun unloadModel() {
        try {
            if (LlamaInterface.isModelLoaded()) {
                Log.i(TAG, "Unloading model")
                LlamaInterface.unloadModel()
                currentModelPath = null
                
                // Force garbage collection to free memory
                System.gc()
                Log.i(TAG, "Model unloaded and memory cleaned")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unloading model", e)
        }
    }

    private fun isValidModelFile(file: File): Boolean {
        return try {
            file.exists() && 
            file.length() > 0 && 
            (file.extension.lowercase() == "gguf" || file.name.contains("gguf"))
        } catch (e: Exception) {
            Log.e(TAG, "Error validating model file", e)
            false
        }
    }

    private fun getOrCopyModelToCache(context: Context, uri: Uri): File? {
        return try {
            val fileName = getFileNameFromUri(context, uri) ?: "model.gguf"
            val cachedFile = File(context.cacheDir, fileName)
            
            // Check if file already exists and has reasonable size
            if (cachedFile.exists() && cachedFile.length() > 1024) {
                Log.i(TAG, "Using cached model file: ${cachedFile.name} (${cachedFile.length() / 1024 / 1024}MB)")
                return cachedFile
            }
            
            Log.i(TAG, "Copying model file to cache: $fileName")
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null

            // Check available cache space
            val cacheDir = context.cacheDir
            val usableSpace = cacheDir.usableSpace
            Log.i(TAG, "Available cache space: ${usableSpace / 1024 / 1024}MB")

            FileOutputStream(cachedFile).use { output ->
                inputStream.copyTo(output)
            }

            Log.i(TAG, "Model file copied successfully. Size: ${cachedFile.length() / 1024 / 1024}MB")
            cachedFile
        } catch (e: Exception) {
            Log.e(TAG, "Error copying model to cache", e)
            null
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        name = it.getString(index)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file name from URI", e)
        }
        return name
    }

    private fun extractContextSize(info: String): Int {
        return try {
            val regex = Regex("Context Size: (\\d+)")
            regex.find(info)?.groupValues?.get(1)?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun extractVocabSize(info: String): Int {
        return try {
            val regex = Regex("Vocab Size: (\\d+)")
            regex.find(info)?.groupValues?.get(1)?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    // Clean up when app is closing
    fun cleanup() {
        Log.i(TAG, "Cleaning up LlamaRunner")
        try {
            unloadModel()
            currentModelPath = null
            modelUri = null
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}
