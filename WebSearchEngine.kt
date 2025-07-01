package com.prelightwight.aibrother.network

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import info.guardianproject.netcipher.client.StrongOkHttpClientBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class SearchResult(
    val id: String,
    val title: String,
    val url: String,
    val snippet: String,
    val timestamp: Long,
    val source: SearchSource
)

enum class SearchSource(val displayName: String) {
    CLEARNET("Clearnet"),
    TOR("Tor Network")
}

data class SearchResponse(
    @SerializedName("results")
    val results: List<WebSearchResult>,
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("query")
    val query: String = ""
)

data class WebSearchResult(
    @SerializedName("title")
    val title: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("snippet")
    val snippet: String,
    @SerializedName("displayUrl")
    val displayUrl: String? = null
)

// Multiple search engine interfaces
interface DuckDuckGoApi {
    @GET("?format=json&no_html=1&skip_disambig=1")
    suspend fun search(
        @Query("q") query: String,
        @Query("safesearch") safeSearch: String = "moderate"
    ): Response<DuckDuckGoResponse>
}

interface SearxApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("engines") engines: String = "google,bing,duckduckgo"
    ): Response<SearxResponse>
}

data class DuckDuckGoResponse(
    @SerializedName("Abstract")
    val abstract: String,
    @SerializedName("AbstractText")
    val abstractText: String,
    @SerializedName("AbstractSource")
    val abstractSource: String,
    @SerializedName("AbstractURL")
    val abstractUrl: String,
    @SerializedName("RelatedTopics")
    val relatedTopics: List<DuckDuckGoResult>,
    @SerializedName("Results")
    val results: List<DuckDuckGoResult>
)

data class DuckDuckGoResult(
    @SerializedName("Text")
    val text: String,
    @SerializedName("FirstURL")
    val firstUrl: String?,
    @SerializedName("Result")
    val result: String?
)

data class SearxResponse(
    @SerializedName("results")
    val results: List<SearxResult>,
    @SerializedName("number_of_results")
    val numberOfResults: Int
)

data class SearxResult(
    @SerializedName("title")
    val title: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("engine")
    val engine: String
)

class WebSearchEngine private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "WebSearchEngine"
        private const val DUCKDUCKGO_BASE_URL = "https://api.duckduckgo.com/"
        private const val SEARX_BASE_URL = "https://searx.org/"
        private const val SEARX_TOR_URL = "https://searx3aolosaf3dmbi7gs6jxhj4kkypwvfzvtrqejqjl3vx7s4w2ad.onion/"
        
        @Volatile
        private var INSTANCE: WebSearchEngine? = null
        
        fun getInstance(context: Context): WebSearchEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = WebSearchEngine(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private var clearnetClient: OkHttpClient? = null
    private var torClient: OkHttpClient? = null
    private var duckDuckGoApi: DuckDuckGoApi? = null
    private var searxClearnetApi: SearxApi? = null
    private var searxTorApi: SearxApi? = null
    
    private val gson = Gson()
    private var isTorAvailable = false

    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Initializing Web Search Engine...")
            
            setupClearnetClient()
            setupTorClient()
            setupApis()
            
            Log.d(TAG, "Web Search Engine initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Web Search Engine", e)
        }
    }

    private fun setupClearnetClient() {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        clearnetClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:91.0) Gecko/91.0 Firefox/91.0")
                    .header("Accept", "application/json, text/html, */*")
                    .header("Accept-Language", "en-US,en;q=0.5")
                
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private suspend fun setupTorClient() = withContext(Dispatchers.IO) {
        try {
            // Use NetCipher for Tor support
            val builder = StrongOkHttpClientBuilder.forMaxSecurity(context)
            
            torClient = builder
                .addInterceptor { chain ->
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0")
                        .header("Accept", "application/json, text/html, */*")
                    
                    val request = requestBuilder.build()
                    chain.proceed(request)
                }
                .connectTimeout(60, TimeUnit.SECONDS)  // Tor is slower
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
            
            isTorAvailable = true
            Log.d(TAG, "Tor client initialized successfully")
        } catch (e: Exception) {
            Log.w(TAG, "Tor not available: ${e.message}")
            isTorAvailable = false
        }
    }

    private fun setupApis() {
        // Setup DuckDuckGo API (Clearnet only)
        clearnetClient?.let { client ->
            val retrofit = Retrofit.Builder()
                .baseUrl(DUCKDUCKGO_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            duckDuckGoApi = retrofit.create(DuckDuckGoApi::class.java)
        }

        // Setup Searx APIs
        clearnetClient?.let { client ->
            val retrofit = Retrofit.Builder()
                .baseUrl(SEARX_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            searxClearnetApi = retrofit.create(SearxApi::class.java)
        }

        if (isTorAvailable) {
            torClient?.let { client ->
                val retrofit = Retrofit.Builder()
                    .baseUrl(SEARX_TOR_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                
                searxTorApi = retrofit.create(SearxApi::class.java)
            }
        }
    }

    suspend fun search(query: String, useTor: Boolean = false): List<SearchResult> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        
        val source = if (useTor) SearchSource.TOR else SearchSource.CLEARNET
        
        try {
            Log.d(TAG, "Searching for '$query' using ${source.displayName}")
            
            val results = if (useTor && isTorAvailable) {
                searchWithTor(query)
            } else {
                searchWithClearnet(query)
            }
            
            Log.d(TAG, "Found ${results.size} results for '$query'")
            results
        } catch (e: Exception) {
            Log.e(TAG, "Search failed for '$query'", e)
            
            // Return fallback results
            listOf(
                SearchResult(
                    id = "error",
                    title = "Search Error",
                    url = "",
                    snippet = "Unable to perform search: ${e.message}. Please check your internet connection and try again.",
                    timestamp = System.currentTimeMillis(),
                    source = source
                )
            )
        }
    }

    private suspend fun searchWithClearnet(query: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        
        // Try multiple search engines for better results
        try {
            // Try Searx first (aggregates multiple engines)
            searxClearnetApi?.let { api ->
                val response = api.search(query)
                if (response.isSuccessful && response.body() != null) {
                    val searxResults = response.body()!!.results.take(5)
                    results.addAll(searxResults.map { result ->
                        SearchResult(
                            id = "searx_${System.nanoTime()}",
                            title = result.title,
                            url = result.url,
                            snippet = result.content.take(200),
                            timestamp = System.currentTimeMillis(),
                            source = SearchSource.CLEARNET
                        )
                    })
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Searx search failed: ${e.message}")
        }

        // If we don't have enough results, try DuckDuckGo
        if (results.size < 3) {
            try {
                duckDuckGoApi?.let { api ->
                    val response = api.search(query)
                    if (response.isSuccessful && response.body() != null) {
                        val ddgResponse = response.body()!!
                        
                        // Add abstract result if available
                        if (ddgResponse.abstractText.isNotBlank()) {
                            results.add(
                                SearchResult(
                                    id = "ddg_abstract",
                                    title = ddgResponse.abstractSource.ifBlank { "Summary" },
                                    url = ddgResponse.abstractUrl.ifBlank { "https://duckduckgo.com/?q=${URLEncoder.encode(query, "UTF-8")}" },
                                    snippet = ddgResponse.abstractText,
                                    timestamp = System.currentTimeMillis(),
                                    source = SearchSource.CLEARNET
                                )
                            )
                        }
                        
                        // Add related topics
                        ddgResponse.relatedTopics.take(4).forEach { topic ->
                            if (topic.text.isNotBlank() && topic.firstUrl != null) {
                                results.add(
                                    SearchResult(
                                        id = "ddg_${System.nanoTime()}",
                                        title = topic.text.split(" - ").firstOrNull() ?: topic.text.take(50),
                                        url = topic.firstUrl,
                                        snippet = topic.text,
                                        timestamp = System.currentTimeMillis(),
                                        source = SearchSource.CLEARNET
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "DuckDuckGo search failed: ${e.message}")
            }
        }

        // If still no results, provide fallback
        if (results.isEmpty()) {
            results.add(
                SearchResult(
                    id = "fallback",
                    title = "Search for \"$query\" on the web",
                    url = "https://duckduckgo.com/?q=${URLEncoder.encode(query, "UTF-8")}",
                    snippet = "Click to open DuckDuckGo search for \"$query\". This will open in your default browser.",
                    timestamp = System.currentTimeMillis(),
                    source = SearchSource.CLEARNET
                )
            )
        }

        return results.take(5) // Limit to 5 results
    }

    private suspend fun searchWithTor(query: String): List<SearchResult> {
        if (!isTorAvailable) {
            throw IOException("Tor network not available")
        }

        val results = mutableListOf<SearchResult>()
        
        try {
            searxTorApi?.let { api ->
                val response = api.search(query)
                if (response.isSuccessful && response.body() != null) {
                    val searxResults = response.body()!!.results.take(3) // Fewer results via Tor
                    results.addAll(searxResults.map { result ->
                        SearchResult(
                            id = "tor_${System.nanoTime()}",
                            title = result.title,
                            url = result.url,
                            snippet = result.content.take(200),
                            timestamp = System.currentTimeMillis(),
                            source = SearchSource.TOR
                        )
                    })
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Tor search failed: ${e.message}")
            throw e
        }

        // Provide fallback if no results
        if (results.isEmpty()) {
            results.add(
                SearchResult(
                    id = "tor_fallback",
                    title = "Anonymous search for \"$query\"",
                    url = "${SEARX_TOR_URL}search?q=${URLEncoder.encode(query, "UTF-8")}",
                    snippet = "Search completed anonymously via Tor network. Results may be limited for privacy.",
                    timestamp = System.currentTimeMillis(),
                    source = SearchSource.TOR
                )
            )
        }

        return results
    }

    fun isTorSupported(): Boolean = isTorAvailable

    suspend fun testConnection(useTor: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        try {
            val client = if (useTor && isTorAvailable) torClient else clearnetClient
            client ?: return@withContext false
            
            val request = Request.Builder()
                .url(if (useTor) SEARX_TOR_URL else "https://duckduckgo.com/")
                .head() // Just check connectivity
                .build()
            
            val response = client.newCall(request).execute()
            val isSuccessful = response.isSuccessful
            response.close()
            
            Log.d(TAG, "${if (useTor) "Tor" else "Clearnet"} connection test: ${if (isSuccessful) "SUCCESS" else "FAILED"}")
            isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "${if (useTor) "Tor" else "Clearnet"} connection test failed", e)
            false
        }
    }

    suspend fun getSearchSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        if (query.length < 2) return@withContext emptyList()
        
        try {
            // Use DuckDuckGo suggestions API
            clearnetClient?.let { client ->
                val url = "https://duckduckgo.com/ac/?q=${URLEncoder.encode(query, "UTF-8")}&type=list"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    response.close()
                    
                    if (json != null) {
                        try {
                            val suggestions = gson.fromJson(json, Array<String>::class.java)
                            return@withContext suggestions.take(5).toList()
                        } catch (e: JsonSyntaxException) {
                            Log.w(TAG, "Failed to parse suggestions: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get search suggestions", e)
        }
        
        emptyList()
    }

    fun cleanup() {
        clearnetClient?.dispatcher?.executorService?.shutdown()
        torClient?.dispatcher?.executorService?.shutdown()
        
        clearnetClient = null
        torClient = null
        duckDuckGoApi = null
        searxClearnetApi = null
        searxTorApi = null
    }
}