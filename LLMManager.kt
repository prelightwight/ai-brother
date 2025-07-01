package com.prelightwight.aibrother.llm

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.*
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

data class LLMModel(
    val id: String,
    val name: String,
    val description: String,
    val size: String,
    val parameters: String,
    val quantization: String,
    val downloadUrl: String,
    val filename: String,
    val isRecommended: Boolean = false,
    val tags: List<String> = emptyList(),
    val author: String = "",
    val license: String = "Apache 2.0"
)

data class DownloadProgress(
    val modelId: String,
    val progress: Float = 0f,
    val isDownloading: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speed: String = ""
)

data class LocalModel(
    val id: String,
    val name: String,
    val filepath: String,
    val size: Long,
    val dateAdded: Date,
    val isActive: Boolean = false,
    val isFromDownload: Boolean = false
)

sealed class LLMManagerState {
    object Loading : LLMManagerState()
    data class Ready(
        val availableModels: List<LLMModel>,
        val localModels: List<LocalModel>,
        val activeModel: LocalModel?
    ) : LLMManagerState()
    data class Error(val message: String) : LLMManagerState()
}

class LLMManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "LLMManager"
        
        @Volatile
        private var INSTANCE: LLMManager? = null
        
        fun getInstance(context: Context): LLMManager {
            return INSTANCE ?: synchronized(this) {
                val instance = LLMManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val modelsDirectory = File(context.filesDir, "llm_models")
    private val preferencesFile = File(context.filesDir, "llm_preferences.json")

    private val _state = MutableStateFlow<LLMManagerState>(LLMManagerState.Loading)
    val state: StateFlow<LLMManagerState> = _state.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, DownloadProgress>> = _downloadProgress.asStateFlow()

    init {
        if (!modelsDirectory.exists()) {
            modelsDirectory.mkdirs()
        }
    }

    // Curated list of high-quality open-source LLMs
    private val curatedModels = listOf(
        LLMModel(
            id = "nous_hermes_2_mistral_7b",
            name = "Nous Hermes 2 - Mistral 7B",
            description = "Excellent general-purpose model with strong reasoning and instruction following. Great for beginners.",
            size = "4.1 GB",
            parameters = "7B",
            quantization = "Q4_K_M",
            downloadUrl = "https://huggingface.co/TheBloke/Nous-Hermes-2-Mistral-7B-DPO-GGUF/resolve/main/nous-hermes-2-mistral-7b-dpo.Q4_K_M.gguf",
            filename = "nous-hermes-2-mistral-7b-dpo.Q4_K_M.gguf",
            isRecommended = true,
            tags = listOf("General Purpose", "Instruction Following", "Beginner Friendly"),
            author = "NousResearch",
            license = "Apache 2.0"
        ),
        LLMModel(
            id = "openhermes_2_5_mistral",
            name = "OpenHermes 2.5 Mistral 7B",
            description = "Enhanced version with improved training data. Excellent for conversations and coding assistance.",
            size = "4.1 GB",
            parameters = "7B",
            quantization = "Q4_K_M",
            downloadUrl = "https://huggingface.co/TheBloke/OpenHermes-2.5-Mistral-7B-GGUF/resolve/main/openhermes-2.5-mistral-7b.Q4_K_M.gguf",
            filename = "openhermes-2.5-mistral-7b.Q4_K_M.gguf",
            isRecommended = true,
            tags = listOf("Conversation", "Coding", "Enhanced Training"),
            author = "teknium",
            license = "Apache 2.0"
        ),
        LLMModel(
            id = "mythomax_l2_13b",
            name = "MythoMax-L2 13B",
            description = "Creative writing specialist based on LLaMA 2. Perfect for storytelling and creative tasks.",
            size = "7.3 GB",
            parameters = "13B",
            quantization = "Q4_K_M",
            downloadUrl = "https://huggingface.co/TheBloke/MythoMax-L2-13B-GGUF/resolve/main/mythomax-l2-13b.Q4_K_M.gguf",
            filename = "mythomax-l2-13b.Q4_K_M.gguf",
            isRecommended = false,
            tags = listOf("Creative Writing", "Storytelling", "Large Model"),
            author = "Gryphe",
            license = "LLaMA 2 Custom"
        ),
        LLMModel(
            id = "chronos_hermes_13b",
            name = "Chronos-Hermes 13B",
            description = "Time-aware model with excellent reasoning capabilities. Great for complex problem solving.",
            size = "7.3 GB",
            parameters = "13B",
            quantization = "Q4_K_M",
            downloadUrl = "https://huggingface.co/TheBloke/Chronos-Hermes-13B-v2-GGUF/resolve/main/chronos-hermes-13b-v2.Q4_K_M.gguf",
            filename = "chronos-hermes-13b-v2.Q4_K_M.gguf",
            isRecommended = false,
            tags = listOf("Problem Solving", "Reasoning", "Large Model"),
            author = "Austism",
            license = "LLaMA 2 Custom"
        ),
        LLMModel(
            id = "dolphin_mistral_7b_2_6",
            name = "Dolphin 2.6 Mistral 7B",
            description = "Uncensored model with strong performance across various tasks. Advanced users only.",
            size = "4.1 GB",
            parameters = "7B",
            quantization = "Q4_K_M",
            downloadUrl = "https://huggingface.co/TheBloke/dolphin-2.6-mistral-7B-GGUF/resolve/main/dolphin-2.6-mistral-7b.Q4_K_M.gguf",
            filename = "dolphin-2.6-mistral-7b.Q4_K_M.gguf",
            isRecommended = false,
            tags = listOf("Uncensored", "Advanced", "Multi-Task"),
            author = "cognitivecomputations",
            license = "Apache 2.0"
        ),
        LLMModel(
            id = "dolphin_mistral_7b_2_7",
            name = "Dolphin 2.7 Mistral 7B",
            description = "Latest Dolphin version with improved capabilities and training refinements.",
            size = "4.1 GB",
            parameters = "7B",
            quantization = "Q4_K_M",
            downloadUrl = "https://huggingface.co/TheBloke/dolphin-2.7-mistral-7b-GGUF/resolve/main/dolphin-2.7-mistral-7b.Q4_K_M.gguf",
            filename = "dolphin-2.7-mistral-7b.Q4_K_M.gguf",
            isRecommended = true,
            tags = listOf("Latest", "Improved", "Multi-Task"),
            author = "cognitivecomputations",
            license = "Apache 2.0"
        ),
        LLMModel(
            id = "phi_2_2_7b",
            name = "Microsoft Phi-2 (2.7B)",
            description = "Compact but powerful model from Microsoft. Great for devices with limited RAM.",
            size = "1.6 GB",
            parameters = "2.7B",
            quantization = "Q4_K_M",
            downloadUrl = "https://huggingface.co/TheBloke/phi-2-GGUF/resolve/main/phi-2.Q4_K_M.gguf",
            filename = "phi-2.Q4_K_M.gguf",
            isRecommended = true,
            tags = listOf("Compact", "Low RAM", "Microsoft", "Efficient"),
            author = "Microsoft",
            license = "MIT"
        ),
        LLMModel(
            id = "mistral_7b_instruct",
            name = "Mistral 7B Instruct v0.2",
            description = "Official Mistral model optimized for instruction following. Excellent baseline model.",
            size = "4.1 GB",
            parameters = "7B",
            quantization = "Q4_K_M",
            downloadUrl = "https://huggingface.co/TheBloke/Mistral-7B-Instruct-v0.2-GGUF/resolve/main/mistral-7b-instruct-v0.2.Q4_K_M.gguf",
            filename = "mistral-7b-instruct-v0.2.Q4_K_M.gguf",
            isRecommended = true,
            tags = listOf("Official", "Baseline", "Instruction Following"),
            author = "Mistral AI",
            license = "Apache 2.0"
        ),
        LLMModel(
            id = "codellama_7b_instruct",
            name = "Code Llama 7B Instruct",
            description = "Specialized coding assistant based on LLaMA 2. Perfect for programming tasks.",
            size = "3.8 GB",
            parameters = "7B",
            quantization = "Q4_K_M",
            downloadUrl = "https://huggingface.co/TheBloke/CodeLlama-7B-Instruct-GGUF/resolve/main/codellama-7b-instruct.Q4_K_M.gguf",
            filename = "codellama-7b-instruct.Q4_K_M.gguf",
            isRecommended = false,
            tags = listOf("Coding", "Programming", "Meta", "Specialized"),
            author = "Meta",
            license = "LLaMA 2 Custom"
        ),
        LLMModel(
            id = "neural_chat_7b",
            name = "Neural Chat 7B v3.3",
            description = "Intel's optimized chat model with excellent performance and efficiency.",
            size = "4.1 GB",
            parameters = "7B",
            quantization = "Q4_K_M",
            downloadUrl = "https://huggingface.co/TheBloke/neural-chat-7B-v3-3-GGUF/resolve/main/neural-chat-7b-v3-3.Q4_K_M.gguf",
            filename = "neural-chat-7b-v3-3.Q4_K_M.gguf",
            isRecommended = false,
            tags = listOf("Intel", "Optimized", "Chat", "Efficient"),
            author = "Intel",
            license = "Apache 2.0"
        )
    )

    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Initializing LLM Manager...")
            
            val localModels = scanLocalModels()
            val activeModel = loadActiveModel()
            
            _state.value = LLMManagerState.Ready(
                availableModels = curatedModels,
                localModels = localModels,
                activeModel = activeModel
            )
            
            Log.d(TAG, "LLM Manager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize LLM Manager", e)
            _state.value = LLMManagerState.Error("Failed to initialize: ${e.message}")
        }
    }

    private suspend fun scanLocalModels(): List<LocalModel> = withContext(Dispatchers.IO) {
        try {
            val models = mutableListOf<LocalModel>()
            
            if (modelsDirectory.exists()) {
                modelsDirectory.listFiles()?.forEach { file ->
                    if (file.isFile && (file.extension == "gguf" || file.extension == "bin")) {
                        val model = LocalModel(
                            id = file.nameWithoutExtension,
                            name = file.nameWithoutExtension.replace("-", " ").replace("_", " "),
                            filepath = file.absolutePath,
                            size = file.length(),
                            dateAdded = Date(file.lastModified()),
                            isFromDownload = curatedModels.any { it.filename == file.name }
                        )
                        models.add(model)
                    }
                }
            }
            
            models.sortedByDescending { it.dateAdded }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning local models", e)
            emptyList()
        }
    }

    private fun loadActiveModel(): LocalModel? {
        return try {
            // TODO: Load from preferences
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error loading active model", e)
            null
        }
    }

    suspend fun downloadModel(model: LLMModel) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting download for ${model.name}")
            
            // Update download state
            updateDownloadProgress(model.id) { 
                it.copy(isDownloading = true, error = null)
            }

            val request = Request.Builder()
                .url(model.downloadUrl)
                .build()

            val response = okHttpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                throw IOException("Download failed: ${response.code}")
            }

            val body = response.body ?: throw IOException("Empty response body")
            val contentLength = body.contentLength()
            
            val outputFile = File(modelsDirectory, model.filename)
            val sink = outputFile.sink().buffer()
            
            var totalBytesRead = 0L
            var lastProgressUpdate = System.currentTimeMillis()
            var lastBytesRead = 0L
            
            body.source().use { source ->
                sink.use { bufferedSink ->
                    val buffer = okio.Buffer()
                    var bytesRead: Long
                    
                    while (source.read(buffer, 8192).also { bytesRead = it } != -1L) {
                        bufferedSink.write(buffer, bytesRead)
                        totalBytesRead += bytesRead
                        
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastProgressUpdate > 1000) { // Update every second
                            val speed = calculateDownloadSpeed(totalBytesRead - lastBytesRead, currentTime - lastProgressUpdate)
                            val progress = if (contentLength > 0) totalBytesRead.toFloat() / contentLength else 0f
                            
                            updateDownloadProgress(model.id) { 
                                it.copy(
                                    progress = progress,
                                    downloadedBytes = totalBytesRead,
                                    totalBytes = contentLength,
                                    speed = speed
                                )
                            }
                            
                            lastProgressUpdate = currentTime
                            lastBytesRead = totalBytesRead
                        }
                    }
                }
            }

            // Mark as completed
            updateDownloadProgress(model.id) { 
                it.copy(
                    isDownloading = false, 
                    isCompleted = true, 
                    progress = 1f,
                    downloadedBytes = totalBytesRead,
                    totalBytes = contentLength
                )
            }

            // Refresh local models
            refreshLocalModels()
            
            Log.d(TAG, "Download completed for ${model.name}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Download failed for ${model.name}", e)
            updateDownloadProgress(model.id) { 
                it.copy(
                    isDownloading = false, 
                    error = e.message ?: "Download failed"
                )
            }
            throw e
        }
    }

    private fun calculateDownloadSpeed(bytes: Long, timeMs: Long): String {
        if (timeMs == 0L) return "0 KB/s"
        
        val bytesPerSecond = (bytes * 1000) / timeMs
        return when {
            bytesPerSecond >= 1_000_000 -> "${(bytesPerSecond / 1_000_000.0).roundToInt()} MB/s"
            bytesPerSecond >= 1_000 -> "${(bytesPerSecond / 1_000.0).roundToInt()} KB/s"
            else -> "$bytesPerSecond B/s"
        }
    }

    private fun updateDownloadProgress(modelId: String, update: (DownloadProgress) -> DownloadProgress) {
        val currentProgress = _downloadProgress.value
        val existingProgress = currentProgress[modelId] ?: DownloadProgress(modelId)
        val updatedProgress = update(existingProgress)
        _downloadProgress.value = currentProgress + (modelId to updatedProgress)
    }

    suspend fun cancelDownload(modelId: String) {
        // TODO: Implement download cancellation
        updateDownloadProgress(modelId) { 
            it.copy(isDownloading = false, error = "Cancelled by user")
        }
    }

    suspend fun deleteModel(localModel: LocalModel): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(localModel.filepath)
            val deleted = file.delete()
            
            if (deleted) {
                refreshLocalModels()
                Log.d(TAG, "Deleted model: ${localModel.name}")
            }
            
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting model: ${localModel.name}", e)
            false
        }
    }

    suspend fun setActiveModel(localModel: LocalModel): Boolean = withContext(Dispatchers.IO) {
        try {
            // Initialize LlamaRunner with the selected model
            val uri = Uri.fromFile(File(localModel.filepath))
            LlamaRunner.setModelUri(uri)
            LlamaRunner.init(context)
            
            // TODO: Save to preferences
            
            // Update state
            val currentState = _state.value
            if (currentState is LLMManagerState.Ready) {
                val updatedLocalModels = currentState.localModels.map { model ->
                    model.copy(isActive = model.id == localModel.id)
                }
                
                _state.value = currentState.copy(
                    localModels = updatedLocalModels,
                    activeModel = localModel.copy(isActive = true)
                )
            }
            
            Log.d(TAG, "Set active model: ${localModel.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting active model: ${localModel.name}", e)
            false
        }
    }

    suspend fun addLocalModelFromFile(uri: Uri, displayName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext false
            
            // Generate a safe filename
            val safeFilename = displayName.replace("[^a-zA-Z0-9.-]".toRegex(), "_") + 
                if (!displayName.endsWith(".gguf")) ".gguf" else ""
            
            val outputFile = File(modelsDirectory, safeFilename)
            
            inputStream.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            refreshLocalModels()
            Log.d(TAG, "Added local model: $displayName")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding local model: $displayName", e)
            false
        }
    }

    private suspend fun refreshLocalModels() {
        val currentState = _state.value
        if (currentState is LLMManagerState.Ready) {
            val localModels = scanLocalModels()
            _state.value = currentState.copy(localModels = localModels)
        }
    }

    fun getRecommendedModels(): List<LLMModel> {
        return curatedModels.filter { it.isRecommended }
    }

    fun getAllModels(): List<LLMModel> {
        return curatedModels
    }

    fun getModelsByTag(tag: String): List<LLMModel> {
        return curatedModels.filter { model ->
            model.tags.any { it.equals(tag, ignoreCase = true) }
        }
    }

    fun isModelDownloaded(model: LLMModel): Boolean {
        val file = File(modelsDirectory, model.filename)
        return file.exists() && file.length() > 0
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000 -> "${(bytes / 1_000_000_000.0).let { "%.1f".format(it) }} GB"
            bytes >= 1_000_000 -> "${(bytes / 1_000_000.0).let { "%.1f".format(it) }} MB"
            bytes >= 1_000 -> "${(bytes / 1_000.0).let { "%.1f".format(it) }} KB"
            else -> "$bytes B"
        }
    }

    fun cleanup() {
        try {
            okHttpClient.dispatcher.executorService.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}