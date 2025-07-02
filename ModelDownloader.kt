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

// Legacy ModelInfo for backward compatibility
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
) {
    companion object {
        fun fromCatalogModel(catalogModel: CatalogModelInfo): ModelInfo {
            return ModelInfo(
                id = catalogModel.id,
                name = catalogModel.name,
                description = catalogModel.description,
                size = catalogModel.sizeDisplay,
                downloadUrl = catalogModel.downloadUrls.firstOrNull()?.url ?: "",
                filename = catalogModel.filename,
                parameters = catalogModel.parameters,
                quantization = catalogModel.quantization,
                contextLength = catalogModel.contextLength,
                useCase = catalogModel.useCase
            )
        }
    }
}

data class DownloadProgress(
    val modelId: String,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val percentage: Int,
    val status: DownloadStatus,
    val speed: Long = 0, // bytes per second
    val eta: Long = 0, // estimated time remaining in seconds
    val currentMirror: String = ""
)

enum class DownloadStatus {
    PENDING, DOWNLOADING, COMPLETED, FAILED, CANCELLED, VERIFYING
}

class ModelDownloader(private val context: Context) {
    
    companion object {
        private const val TAG = "ModelDownloader"
        private const val MODELS_DIR = "models"
    }
    
    private val modelCatalog = ModelCatalog(context)
    private var cachedCatalogModels: List<CatalogModelInfo>? = null

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

    suspend fun getAvailableModels(): List<ModelInfo> {
        return try {
            val catalog = modelCatalog.fetchModelCatalog()
            cachedCatalogModels = catalog.models
            catalog.models.map { ModelInfo.fromCatalogModel(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch model catalog", e)
            // Return empty list if catalog fails
            emptyList()
        }
    }

    suspend fun getDownloadedModels(): List<ModelInfo> {
        val available = getAvailableModels()
        return available.filter { model ->
            File(modelsDir, model.filename).exists()
        }
    }

    fun isModelDownloaded(modelId: String): Boolean {
        val catalogModel = cachedCatalogModels?.find { it.id == modelId }
        if (catalogModel != null) {
            return File(modelsDir, catalogModel.filename).exists()
        }
        return false
    }

    fun getModelFile(modelId: String): File? {
        val catalogModel = cachedCatalogModels?.find { it.id == modelId } ?: return null
        val file = File(modelsDir, catalogModel.filename)
        return if (file.exists()) file else null
    }

    fun downloadModel(modelId: String): Flow<DownloadProgress> = flow {
        // Get model from catalog
        val catalogModel = cachedCatalogModels?.find { it.id == modelId }
            ?: run {
                // Try to refresh catalog if model not found
                val catalog = modelCatalog.fetchModelCatalog()
                cachedCatalogModels = catalog.models
                catalog.models.find { it.id == modelId }
            }
            ?: throw IllegalArgumentException("Model not found: $modelId")

        val outputFile = File(modelsDir, catalogModel.filename)
        val tempFile = File(modelsDir, "${catalogModel.filename}.tmp")

        // Skip download if already exists and verified
        if (outputFile.exists()) {
            Log.i(TAG, "Model ${catalogModel.name} already exists, verifying integrity...")
            emit(DownloadProgress(modelId, outputFile.length(), outputFile.length(), 100, DownloadStatus.VERIFYING))
            
            if (modelCatalog.verifyFileIntegrity(outputFile, catalogModel.sha256)) {
                Log.i(TAG, "Model ${catalogModel.name} already downloaded and verified")
                emit(DownloadProgress(modelId, outputFile.length(), outputFile.length(), 100, DownloadStatus.COMPLETED))
                return@flow
            } else {
                Log.w(TAG, "Model ${catalogModel.name} failed integrity check, re-downloading")
                outputFile.delete()
            }
        }

        try {
            emit(DownloadProgress(modelId, 0, 0, 0, DownloadStatus.PENDING))

            // Try mirrors in order of priority
            val mirrors = catalogModel.downloadUrls.sortedBy { it.priority }
            var lastException: Exception? = null
            
            for (mirror in mirrors) {
                try {
                    Log.i(TAG, "Attempting download of ${catalogModel.name} from ${mirror.name} (${mirror.url})")
                    
                    downloadFromMirror(
                        catalogModel, 
                        mirror, 
                        tempFile, 
                        outputFile,
                        modelId
                    ).collect { progress ->
                        emit(progress)
                    }
                    
                    // If we reach here, download was successful
                    return@flow
                    
                } catch (e: Exception) {
                    Log.w(TAG, "Download failed from ${mirror.name}: ${e.message}")
                    lastException = e
                    tempFile.delete()
                    
                    // Continue to next mirror
                }
            }
            
            // If all mirrors failed
            Log.e(TAG, "All download mirrors failed for ${catalogModel.name}")
            emit(DownloadProgress(modelId, 0, 0, 0, DownloadStatus.FAILED))
            throw lastException ?: IOException("All download mirrors failed")

        } catch (e: Exception) {
            Log.e(TAG, "Download failed for ${catalogModel.name}", e)
            tempFile.delete()
            emit(DownloadProgress(modelId, 0, 0, 0, DownloadStatus.FAILED))
            throw e
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun downloadFromMirror(
        catalogModel: CatalogModelInfo,
        mirror: DownloadMirror,
        tempFile: File,
        outputFile: File,
        modelId: String
    ): Flow<DownloadProgress> = flow {
        val request = Request.Builder()
            .url(mirror.url)
            .build()

        val response = client.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}: ${response.message}")
        }

        val totalBytes = response.body?.contentLength() ?: catalogModel.sizeBytes
        var downloadedBytes = 0L
        var startTime = System.currentTimeMillis()
        var lastEmitTime = startTime

        emit(DownloadProgress(modelId, 0, totalBytes, 0, DownloadStatus.DOWNLOADING, currentMirror = mirror.name))

        response.body?.let { body ->
            body.byteStream().use { input ->
                tempFile.outputStream().use { output ->
                    val buffer = ByteArray(16384) // Larger buffer for better performance
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        val currentTime = System.currentTimeMillis()
                        
                        // Emit progress every 500ms or 5MB, whichever comes first
                        if (currentTime - lastEmitTime > 500 || downloadedBytes % (5 * 1024 * 1024) == 0L) {
                            val percentage = if (totalBytes > 0) {
                                ((downloadedBytes * 100) / totalBytes).toInt()
                            } else 0

                            // Calculate speed and ETA
                            val elapsedSeconds = (currentTime - startTime) / 1000.0
                            val speed = if (elapsedSeconds > 0) (downloadedBytes / elapsedSeconds).toLong() else 0L
                            val eta = if (speed > 0 && totalBytes > downloadedBytes) {
                                ((totalBytes - downloadedBytes) / speed)
                            } else 0L

                            emit(DownloadProgress(
                                modelId, 
                                downloadedBytes, 
                                totalBytes, 
                                percentage, 
                                DownloadStatus.DOWNLOADING,
                                speed = speed,
                                eta = eta,
                                currentMirror = mirror.name
                            ))
                            lastEmitTime = currentTime
                        }
                    }
                }
            }
        } ?: throw IOException("Empty response body from ${mirror.name}")

        // Verify integrity before final move
        emit(DownloadProgress(modelId, downloadedBytes, totalBytes, 100, DownloadStatus.VERIFYING, currentMirror = mirror.name))
        
        if (!modelCatalog.verifyFileIntegrity(tempFile, catalogModel.sha256)) {
            throw IOException("Downloaded file failed integrity verification")
        }

        // Move temp file to final location
        if (tempFile.renameTo(outputFile)) {
            Log.i(TAG, "Successfully downloaded and verified ${catalogModel.name} from ${mirror.name} (${formatBytes(downloadedBytes)})")
            emit(DownloadProgress(modelId, downloadedBytes, totalBytes, 100, DownloadStatus.COMPLETED, currentMirror = mirror.name))
        } else {
            throw IOException("Failed to move downloaded file")
        }
    }

    suspend fun deleteModel(modelId: String): Boolean = withContext(Dispatchers.IO) {
        val catalogModel = cachedCatalogModels?.find { it.id == modelId } ?: return@withContext false
        val file = File(modelsDir, catalogModel.filename)
        
        return@withContext if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                Log.i(TAG, "Deleted model: ${catalogModel.name}")
            }
            deleted
        } else {
            false
        }
    }

    suspend fun getModelStorageInfo(): Map<String, Any> {
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