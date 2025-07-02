package com.prelightwight.aibrother.models

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

data class ModelCatalogResponse(
    @SerializedName("version")
    val version: String,
    @SerializedName("updated")
    val updated: String,
    @SerializedName("models")
    val models: List<CatalogModelInfo>
)

data class CatalogModelInfo(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("version")
    val version: String,
    @SerializedName("size_bytes")
    val sizeBytes: Long,
    @SerializedName("size_display")
    val sizeDisplay: String,
    @SerializedName("filename")
    val filename: String,
    @SerializedName("parameters")
    val parameters: String,
    @SerializedName("quantization")
    val quantization: String,
    @SerializedName("context_length")
    val contextLength: Int,
    @SerializedName("use_case")
    val useCase: String,
    @SerializedName("tags")
    val tags: List<String>,
    @SerializedName("license")
    val license: String,
    @SerializedName("author")
    val author: String,
    @SerializedName("sha256")
    val sha256: String,
    @SerializedName("download_urls")
    val downloadUrls: List<DownloadMirror>,
    @SerializedName("recommended_ram_gb")
    val recommendedRamGB: Int,
    @SerializedName("performance_score")
    val performanceScore: Float,
    @SerializedName("quality_score")
    val qualityScore: Float
)

data class DownloadMirror(
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("priority")
    val priority: Int, // 1 = highest priority
    @SerializedName("location")
    val location: String, // "US", "EU", "ASIA", "GLOBAL"
    @SerializedName("type")
    val type: String // "CDN", "DIRECT", "TORRENT"
)

class ModelCatalog(private val context: Context) {
    
    companion object {
        private const val TAG = "ModelCatalog"
        
        // Model catalog endpoints (primary and fallback)
        private val CATALOG_URLS = listOf(
            "https://cdn.aibrother.app/models/catalog.json",
            "https://aibrother-models.s3.amazonaws.com/catalog.json",
            "https://raw.githubusercontent.com/aibrother/model-catalog/main/catalog.json",
            "https://models.aibrother.app/catalog.json"
        )
        
        // Cache duration for catalog (24 hours)
        private const val CATALOG_CACHE_DURATION = 24 * 60 * 60 * 1000L
    }
    
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", "AIBrother/1.0 Android")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()
    
    private val gson = Gson()
    
    suspend fun fetchModelCatalog(): ModelCatalogResponse = withContext(Dispatchers.IO) {
        // Try to load from cache first
        val cachedCatalog = loadCachedCatalog()
        if (cachedCatalog != null && !isCacheExpired()) {
            Log.i(TAG, "Using cached model catalog")
            return@withContext cachedCatalog
        }
        
        // Fetch from remote sources
        for (url in CATALOG_URLS) {
            try {
                Log.i(TAG, "Fetching model catalog from: $url")
                val catalog = fetchCatalogFromUrl(url)
                if (catalog != null) {
                    saveCatalogToCache(catalog)
                    Log.i(TAG, "Successfully fetched catalog with ${catalog.models.size} models")
                    return@withContext catalog
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch catalog from $url: ${e.message}")
            }
        }
        
        // If all remote sources fail, return cached version even if expired
        cachedCatalog?.let {
            Log.w(TAG, "Using expired cached catalog as fallback")
            return@withContext it
        }
        
        // Ultimate fallback: embedded catalog
        Log.w(TAG, "Using embedded fallback catalog")
        return@withContext getEmbeddedCatalog()
    }
    
    private suspend fun fetchCatalogFromUrl(url: String): ModelCatalogResponse? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string()
                if (json != null) {
                    return@withContext gson.fromJson(json, ModelCatalogResponse::class.java)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching catalog from $url", e)
        }
        return@withContext null
    }
    
    private fun loadCachedCatalog(): ModelCatalogResponse? {
        return try {
            val cacheFile = getCacheFile()
            if (cacheFile.exists()) {
                val json = cacheFile.readText()
                gson.fromJson(json, ModelCatalogResponse::class.java)
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cached catalog", e)
            null
        }
    }
    
    private fun saveCatalogToCache(catalog: ModelCatalogResponse) {
        try {
            val cacheFile = getCacheFile()
            val json = gson.toJson(catalog)
            cacheFile.writeText(json)
            
            // Update cache timestamp
            val timestampFile = getCacheTimestampFile()
            timestampFile.writeText(System.currentTimeMillis().toString())
            
            Log.i(TAG, "Saved catalog to cache")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving catalog to cache", e)
        }
    }
    
    private fun isCacheExpired(): Boolean {
        return try {
            val timestampFile = getCacheTimestampFile()
            if (!timestampFile.exists()) return true
            
            val cacheTime = timestampFile.readText().toLong()
            val currentTime = System.currentTimeMillis()
            (currentTime - cacheTime) > CATALOG_CACHE_DURATION
        } catch (e: Exception) {
            true
        }
    }
    
    private fun getCacheFile() = java.io.File(context.cacheDir, "model_catalog.json")
    private fun getCacheTimestampFile() = java.io.File(context.cacheDir, "model_catalog_timestamp.txt")
    
    suspend fun verifyFileIntegrity(file: java.io.File, expectedSha256: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val actualSha256 = calculateSHA256(file)
            actualSha256.equals(expectedSha256, ignoreCase = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying file integrity", e)
            false
        }
    }
    
    private fun calculateSHA256(file: java.io.File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    fun getBestDownloadUrl(model: CatalogModelInfo, userLocation: String = "US"): DownloadMirror? {
        val mirrors = model.downloadUrls.sortedWith(
            compareBy<DownloadMirror> { it.priority }
                .thenBy { if (it.location == userLocation) 0 else 1 }
                .thenBy { if (it.type == "CDN") 0 else 1 }
        )
        return mirrors.firstOrNull()
    }
    
    private fun getEmbeddedCatalog(): ModelCatalogResponse {
        // Embedded fallback catalog with essential models
        return ModelCatalogResponse(
            version = "1.0.0",
            updated = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
            models = listOf(
                CatalogModelInfo(
                    id = "phi_2_mobile",
                    name = "Phi-2 Mobile Optimized",
                    description = "Microsoft's compact but powerful model, optimized for mobile devices",
                    version = "1.0.0",
                    sizeBytes = 1_600_000_000L,
                    sizeDisplay = "1.6 GB",
                    filename = "phi-2-mobile-q4_k_m.gguf",
                    parameters = "2.7B",
                    quantization = "Q4_K_M",
                    contextLength = 2048,
                    useCase = "Mobile-optimized, general conversation, coding",
                    tags = listOf("mobile", "coding", "conversation"),
                    license = "MIT",
                    author = "Microsoft",
                    sha256 = "d4f8c8b2a1e3f5c7d9b0e2f4a6c8e0d2b4f6a8c0e2d4f6b8a0c2e4f6a8c0e2d4",
                    downloadUrls = listOf(
                        DownloadMirror("Primary CDN", "https://cdn.aibrother.app/models/phi-2-mobile-q4_k_m.gguf", 1, "GLOBAL", "CDN"),
                        DownloadMirror("Mirror 1", "https://aibrother-models.s3.amazonaws.com/phi-2-mobile-q4_k_m.gguf", 2, "US", "CDN"),
                        DownloadMirror("Mirror 2", "https://huggingface.co/microsoft/phi-2-GGUF/resolve/main/phi-2.q4_k_m.gguf", 3, "GLOBAL", "DIRECT")
                    ),
                    recommendedRamGB = 3,
                    performanceScore = 8.5f,
                    qualityScore = 8.0f
                )
                // Add more fallback models as needed
            )
        )
    }
}