package com.prelightwight.aibrother.llm

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class ModelInfo(
    val name: String,
    val displayName: String,
    val description: String,
    val downloadUrl: String,
    val fileName: String,
    val estimatedSizeMB: Int,
    val quantization: String = "Q4_K_M",
    val parameters: String = "",
    val isRecommended: Boolean = false
)

data class DownloadProgress(
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val percentageComplete: Int,
    val downloadSpeedKBps: Long,
    val isComplete: Boolean = false,
    val error: String? = null
)

data class StorageInfo(
    val totalUsedMB: Long,
    val availableMB: Long,
    val downloadedModelsCount: Int
)

class ModelDownloader(private val context: Context) {
    
    companion object {
        private const val TAG = "ModelDownloader"
        private const val MODELS_FOLDER = "models"
    }
    
    fun getModelsDirectory(): File {
        val modelsDir = File(context.getExternalFilesDir(null), MODELS_FOLDER)
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }
        return modelsDir
    }
    
    fun getAvailableModels(): List<ModelInfo> {
        return listOf(
            ModelInfo(
                name = "tinyllama",
                displayName = "TinyLlama 1.1B Chat",
                description = "Ultra-fast, lightweight model perfect for testing and quick responses on mobile devices",
                downloadUrl = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.q4_k_m.gguf",
                fileName = "tinyllama-1.1b-chat-v1.0.q4_k_m.gguf",
                estimatedSizeMB = 669,
                quantization = "Q4_K_M",
                parameters = "1.1B",
                isRecommended = true
            ),
            ModelInfo(
                name = "phi2",
                displayName = "Phi-2 2.7B",
                description = "Microsoft's efficient small model, excellent for general conversation and coding tasks",
                downloadUrl = "https://huggingface.co/microsoft/phi-2-gguf/resolve/main/phi-2.q4_0.gguf",
                fileName = "phi-2.q4_0.gguf",
                estimatedSizeMB = 1600,
                quantization = "Q4_0",
                parameters = "2.7B",
                isRecommended = true
            ),
            ModelInfo(
                name = "gemma2-2b",
                displayName = "Gemma 2 2B Instruct",
                description = "Google's latest compact model with excellent performance for its size",
                downloadUrl = "https://huggingface.co/bartowski/gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q4_K_M.gguf",
                fileName = "gemma-2-2b-it-Q4_K_M.gguf",
                estimatedSizeMB = 1600,
                quantization = "Q4_K_M",
                parameters = "2B",
                isRecommended = true
            ),
            ModelInfo(
                name = "llama32-1b",
                displayName = "Llama 3.2 1B Instruct",
                description = "Meta's latest compact model with excellent instruction following capabilities",
                downloadUrl = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf",
                fileName = "Llama-3.2-1B-Instruct-Q4_K_M.gguf",
                estimatedSizeMB = 800,
                quantization = "Q4_K_M",
                parameters = "1B",
                isRecommended = false
            ),
            ModelInfo(
                name = "qwen25-0.5b",
                displayName = "Qwen 2.5 0.5B Instruct",
                description = "Alibaba's ultra-compact multilingual model, perfect for resource-constrained devices",
                downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
                fileName = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
                estimatedSizeMB = 350,
                quantization = "Q4_K_M",
                parameters = "0.5B",
                isRecommended = false
            ),
            ModelInfo(
                name = "smollm-360m",
                displayName = "SmolLM 360M Instruct",
                description = "Hugging Face's tiny but capable model, ideal for testing and lightweight applications",
                downloadUrl = "https://huggingface.co/HuggingFaceTB/SmolLM-360M-Instruct-GGUF/resolve/main/smollm-360m-instruct-q4_k_m.gguf",
                fileName = "smollm-360m-instruct-q4_k_m.gguf",
                estimatedSizeMB = 220,
                quantization = "Q4_K_M",
                parameters = "360M",
                isRecommended = false
            )
        )
    }
    
    fun isModelDownloaded(model: ModelInfo): Boolean {
        val file = getModelFilePath(model)
        return file.exists() && file.length() > 1024 * 1024 // At least 1MB
    }
    
    fun getModelFilePath(model: ModelInfo): File {
        return File(getModelsDirectory(), model.fileName)
    }
    
    fun getModelStorageInfo(): StorageInfo {
        val modelsDir = getModelsDirectory()
        val totalUsed = modelsDir.listFiles()?.sumOf { it.length() } ?: 0L
        val totalUsedMB = totalUsed / 1024 / 1024
        val downloadedCount = modelsDir.listFiles()?.size ?: 0
        
        val availableMB = (context.getExternalFilesDir(null)?.freeSpace ?: 0L) / 1024 / 1024
        
        return StorageInfo(totalUsedMB, availableMB, downloadedCount)
    }
    
    fun downloadModel(model: ModelInfo): Flow<DownloadProgress> = flow {
        try {
            val targetFile = getModelFilePath(model)
            val tempFile = File(targetFile.absolutePath + ".tmp")
            
            // Clean up any existing temp file
            if (tempFile.exists()) {
                tempFile.delete()
            }
            
            val url = URL(model.downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            
            val totalBytes = connection.contentLength.toLong()
            var downloadedBytes = 0L
            val startTime = System.currentTimeMillis()
            
            val outputStream = FileOutputStream(tempFile)
            val inputStream = connection.inputStream
            
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var lastProgressTime = System.currentTimeMillis()
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                downloadedBytes += bytesRead
                
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastProgressTime >= 1000) { // Update every second
                    val percentage = if (totalBytes > 0) {
                        ((downloadedBytes * 100) / totalBytes).toInt()
                    } else 0
                    
                    val elapsedTime = currentTime - startTime
                    val speedKBps = if (elapsedTime > 0) {
                        (downloadedBytes / elapsedTime)
                    } else 0L
                    
                    emit(DownloadProgress(
                        bytesDownloaded = downloadedBytes,
                        totalBytes = totalBytes,
                        percentageComplete = percentage,
                        downloadSpeedKBps = speedKBps
                    ))
                    
                    lastProgressTime = currentTime
                }
            }
            
            inputStream.close()
            outputStream.close()
            connection.disconnect()
            
            // Move temp file to final location
            if (tempFile.renameTo(targetFile)) {
                emit(DownloadProgress(
                    bytesDownloaded = downloadedBytes,
                    totalBytes = totalBytes,
                    percentageComplete = 100,
                    downloadSpeedKBps = 0,
                    isComplete = true
                ))
            } else {
                emit(DownloadProgress(
                    bytesDownloaded = 0,
                    totalBytes = 0,
                    percentageComplete = 0,
                    downloadSpeedKBps = 0,
                    error = "Failed to finalize download"
                ))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            emit(DownloadProgress(
                bytesDownloaded = 0,
                totalBytes = 0,
                percentageComplete = 0,
                downloadSpeedKBps = 0,
                error = e.message ?: "Unknown error"
            ))
        }
    }.flowOn(Dispatchers.IO)
    
    suspend fun deleteModel(model: ModelInfo): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = getModelFilePath(model)
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete model", e)
            false
        }
    }
}