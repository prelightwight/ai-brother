package com.prelightwight.aibrother.models

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

data class ModelInfo(
    val id: String,
    val name: String,
    val description: String,
    val size: String,
    val downloadUrl: String,
    val filename: String,
    val parameters: String,
    val quantization: String,
    val contextLength: Int,
    val useCase: String
)

data class DownloadProgress(
    val modelId: String,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val percentage: Int,
    val status: DownloadStatus
)

enum class DownloadStatus {
    PENDING, DOWNLOADING, COMPLETED, FAILED, CANCELLED
}

class ModelDownloader(private val context: Context) {
    
    companion object {
        private const val TAG = "ModelDownloader"
        private const val MODELS_DIR = "models"
        
        // Available models with their download URLs and metadata
        val AVAILABLE_MODELS = listOf(
            ModelInfo(
                id = "nous_hermes_2_mistral_7b",
                name = "Nous Hermes 2 - Mistral 7B",
                description = "Fine-tuned on diverse conversation data with excellent instruction following",
                size = "4.1 GB",
                downloadUrl = "https://huggingface.co/NousResearch/Nous-Hermes-2-Mistral-7B-DPO-GGUF/resolve/main/Nous-Hermes-2-Mistral-7B-DPO.Q4_K_M.gguf",
                filename = "nous-hermes-2-mistral-7b-q4_k_m.gguf",
                parameters = "7B",
                quantization = "Q4_K_M",
                contextLength = 8192,
                useCase = "General conversation, instruction following"
            ),
            ModelInfo(
                id = "openhermes_2_5_mistral",
                name = "OpenHermes 2.5 Mistral",
                description = "Enhanced version with improved reasoning and creative writing",
                size = "4.1 GB",
                downloadUrl = "https://huggingface.co/teknium/OpenHermes-2.5-Mistral-7B-GGUF/resolve/main/openhermes-2.5-mistral-7b.q4_k_m.gguf",
                filename = "openhermes-2.5-mistral-7b-q4_k_m.gguf",
                parameters = "7B",
                quantization = "Q4_K_M",
                contextLength = 8192,
                useCase = "Creative writing, complex reasoning"
            ),
            ModelInfo(
                id = "mythomax_l2",
                name = "MythoMax-L2",
                description = "Specialized for creative writing and storytelling, based on LLaMA 2",
                size = "3.8 GB",
                downloadUrl = "https://huggingface.co/TheBloke/MythoMax-L2-13B-GGUF/resolve/main/mythomax-l2-13b.Q4_K_M.gguf",
                filename = "mythomax-l2-13b-q4_k_m.gguf",
                parameters = "13B",
                quantization = "Q4_K_M",
                contextLength = 4096,
                useCase = "Creative writing, storytelling, roleplay"
            ),
            ModelInfo(
                id = "chronos_hermes_13b",
                name = "Chronos-Hermes 13B",
                description = "Balanced model for both creative and analytical tasks",
                size = "7.3 GB",
                downloadUrl = "https://huggingface.co/TheBloke/Chronos-Hermes-13B-GGUF/resolve/main/chronos-hermes-13b.Q4_K_M.gguf",
                filename = "chronos-hermes-13b-q4_k_m.gguf",
                parameters = "13B",
                quantization = "Q4_K_M",
                contextLength = 4096,
                useCase = "Balanced creative and analytical tasks"
            ),
            ModelInfo(
                id = "mistral_7b_dolphin_2_6",
                name = "Mistral 7B - Dolphin 2.6",
                description = "Uncensored model excellent for coding and technical tasks",
                size = "4.1 GB",
                downloadUrl = "https://huggingface.co/TheBloke/dolphin-2.6-mistral-7B-GGUF/resolve/main/dolphin-2.6-mistral-7b.Q4_K_M.gguf",
                filename = "dolphin-2.6-mistral-7b-q4_k_m.gguf",
                parameters = "7B",
                quantization = "Q4_K_M",
                contextLength = 8192,
                useCase = "Coding, technical analysis, uncensored responses"
            ),
            ModelInfo(
                id = "mistral_7b_dolphin_2_7",
                name = "Mistral 7B - Dolphin 2.7",
                description = "Latest Dolphin version with improved reasoning capabilities",
                size = "4.1 GB",
                downloadUrl = "https://huggingface.co/TheBloke/dolphin-2.7-mixtral-8x7b-GGUF/resolve/main/dolphin-2.7-mixtral-8x7b.Q4_K_M.gguf",
                filename = "dolphin-2.7-mistral-7b-q4_k_m.gguf",
                parameters = "7B",
                quantization = "Q4_K_M",
                contextLength = 8192,
                useCase = "Advanced reasoning, coding, analysis"
            ),
            ModelInfo(
                id = "phi_2",
                name = "Phi-2 (2.7B)",
                description = "Compact but powerful model from Microsoft, great for mobile devices",
                size = "1.6 GB",
                downloadUrl = "https://huggingface.co/microsoft/phi-2-GGUF/resolve/main/phi-2.q4_k_m.gguf",
                filename = "phi-2-q4_k_m.gguf",
                parameters = "2.7B",
                quantization = "Q4_K_M",
                contextLength = 2048,
                useCase = "Mobile-optimized, general conversation, coding"
            )
        )
    }

    private val modelsDir: File by lazy {
        File(context.filesDir, MODELS_DIR).apply {
            if (!exists()) mkdirs()
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", "AIBrother/1.0 Android")
                .build()
            chain.proceed(request)
        }
        .build()

    fun getAvailableModels(): List<ModelInfo> = AVAILABLE_MODELS

    fun getDownloadedModels(): List<ModelInfo> {
        return AVAILABLE_MODELS.filter { model ->
            File(modelsDir, model.filename).exists()
        }
    }

    fun isModelDownloaded(modelId: String): Boolean {
        val model = AVAILABLE_MODELS.find { it.id == modelId } ?: return false
        return File(modelsDir, model.filename).exists()
    }

    fun getModelFile(modelId: String): File? {
        val model = AVAILABLE_MODELS.find { it.id == modelId } ?: return null
        val file = File(modelsDir, model.filename)
        return if (file.exists()) file else null
    }

    fun downloadModel(modelId: String): Flow<DownloadProgress> = flow {
        val model = AVAILABLE_MODELS.find { it.id == modelId }
            ?: throw IllegalArgumentException("Model not found: $modelId")

        val outputFile = File(modelsDir, model.filename)
        val tempFile = File(modelsDir, "${model.filename}.tmp")

        try {
            emit(DownloadProgress(modelId, 0, 0, 0, DownloadStatus.PENDING))

            Log.i(TAG, "Starting download of ${model.name} from ${model.downloadUrl}")

            val request = Request.Builder()
                .url(model.downloadUrl)
                .build()

            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                throw IOException("Failed to download: ${response.code} ${response.message}")
            }

            val totalBytes = response.body?.contentLength() ?: -1L
            var downloadedBytes = 0L

            emit(DownloadProgress(modelId, 0, totalBytes, 0, DownloadStatus.DOWNLOADING))

            response.body?.let { body ->
                body.byteStream().use { input ->
                    tempFile.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var lastEmitTime = System.currentTimeMillis()

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead

                            // Emit progress every 100ms or 1MB, whichever comes first
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastEmitTime > 100 || downloadedBytes % (1024 * 1024) == 0L) {
                                val percentage = if (totalBytes > 0) {
                                    ((downloadedBytes * 100) / totalBytes).toInt()
                                } else 0

                                emit(DownloadProgress(
                                    modelId, 
                                    downloadedBytes, 
                                    totalBytes, 
                                    percentage, 
                                    DownloadStatus.DOWNLOADING
                                ))
                                lastEmitTime = currentTime
                            }
                        }
                    }
                }
            } ?: throw IOException("Empty response body")

            // Move temp file to final location
            if (tempFile.renameTo(outputFile)) {
                Log.i(TAG, "Successfully downloaded ${model.name} (${formatBytes(downloadedBytes)})")
                emit(DownloadProgress(modelId, downloadedBytes, totalBytes, 100, DownloadStatus.COMPLETED))
            } else {
                throw IOException("Failed to move downloaded file")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Download failed for ${model.name}", e)
            tempFile.delete()
            emit(DownloadProgress(modelId, 0, 0, 0, DownloadStatus.FAILED))
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun deleteModel(modelId: String): Boolean = withContext(Dispatchers.IO) {
        val model = AVAILABLE_MODELS.find { it.id == modelId } ?: return@withContext false
        val file = File(modelsDir, model.filename)
        
        return@withContext if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                Log.i(TAG, "Deleted model: ${model.name}")
            }
            deleted
        } else {
            false
        }
    }

    fun getModelStorageInfo(): Map<String, Any> {
        val totalSpace = modelsDir.totalSpace
        val freeSpace = modelsDir.freeSpace
        val usedSpace = totalSpace - freeSpace
        
        val downloadedModels = getDownloadedModels()
        val totalModelSize = downloadedModels.sumOf { model ->
            File(modelsDir, model.filename).length()
        }

        return mapOf(
            "totalSpace" to totalSpace,
            "freeSpace" to freeSpace,
            "usedSpace" to usedSpace,
            "modelsDir" to modelsDir.absolutePath,
            "downloadedModels" to downloadedModels.size,
            "totalModelSize" to totalModelSize
        )
    }

    private fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format("%.1f GB", gb)
            mb >= 1 -> String.format("%.1f MB", mb)
            kb >= 1 -> String.format("%.1f KB", kb)
            else -> "$bytes B"
        }
    }

    fun cleanup() {
        // Clean up any temporary files
        modelsDir.listFiles()?.forEach { file ->
            if (file.name.endsWith(".tmp")) {
                file.delete()
                Log.i(TAG, "Cleaned up temp file: ${file.name}")
            }
        }
    }
}