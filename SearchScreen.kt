package com.prelightwight.aibrother.search

import com.prelightwight.aibrother.network.WebSearchEngine
import com.prelightwight.aibrother.data.AppDatabase
import com.prelightwight.aibrother.data.SearchHistoryEntity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

data class SearchQuery(
    val query: String,
    val timestamp: Long,
    val source: SearchSource,
    val resultCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Managers and database
    val searchEngine = remember { WebSearchEngine.getInstance(context) }
    val database = remember { AppDatabase.getDatabase(context) }
    
    // State
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var useTor by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf(listOf<SearchResult>()) }
    var searchHistory by remember { mutableStateOf(listOf<SearchHistoryEntity>()) }
    var showHistory by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf(listOf<String>()) }
    var torAvailable by remember { mutableStateOf(false) }
    
    // Initialize search engine and load history
    LaunchedEffect(Unit) {
        searchEngine.initialize()
        torAvailable = searchEngine.isTorSupported()
        
        // Load search history
        database.searchHistoryDao().getRecentSearches().collect { history ->
            searchHistory = history
        }
    }
    
    // Get search suggestions as user types
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            scope.launch {
                try {
                    suggestions = searchEngine.getSearchSuggestions(searchQuery)
                } catch (e: Exception) {
                    // Ignore suggestion errors
                }
            }
        } else {
            suggestions = emptyList()
        }
    }

    suspend fun performSearch(query: String) {
        if (query.isBlank()) return
        
        isSearching = true
        showHistory = false
        
        try {
            // Perform actual web search
            val results = searchEngine.search(query, useTor)
            searchResults = results.map { networkResult ->
                SearchResult(
                    id = networkResult.id,
                    title = networkResult.title,
                    url = networkResult.url,
                    snippet = networkResult.snippet,
                    timestamp = networkResult.timestamp,
                    source = networkResult.source
                )
            }
            
            // Save to database
            val historyEntry = SearchHistoryEntity(
                query = query,
                source = if (useTor) "TOR" else "CLEARNET",
                resultCount = searchResults.size,
                results = "" // Could store JSON of results if needed
            )
            database.searchHistoryDao().insertSearch(historyEntry)
            
        } catch (e: Exception) {
            // Show error in results
            searchResults = listOf(
                SearchResult(
                    id = "error",
                    title = "Search Error",
                    url = "",
                    snippet = "Unable to perform search: ${e.message}",
                    timestamp = System.currentTimeMillis(),
                    source = if (useTor) SearchSource.TOR else SearchSource.CLEARNET
                )
            )
        } finally {
            isSearching = false
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            text = "🌐 Web Search",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Network selection card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (useTor) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (useTor) Icons.Default.Security else Icons.Default.Language,
                    contentDescription = null,
                    tint = if (useTor) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (useTor) "Tor Network" else "Clearnet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (useTor) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (useTor) "Anonymous browsing enabled" else "Standard web search",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (useTor) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = useTor,
                    onCheckedChange = { useTor = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search the web...") },
            leadingIcon = { 
                Icon(
                    Icons.Default.Search, 
                    contentDescription = null,
                    tint = if (useTor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            trailingIcon = {
                Row {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                    IconButton(onClick = { showHistory = !showHistory }) {
                        Icon(
                            Icons.Default.History, 
                            contentDescription = "Search History",
                            tint = if (showHistory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { performSearch(searchQuery) }
            ),
            enabled = !isSearching
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search suggestions
        if (suggestions.isNotEmpty() && searchQuery.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    suggestions.forEach { suggestion ->
                        TextButton(
                            onClick = { 
                                searchQuery = suggestion
                                scope.launch { performSearch(suggestion) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(suggestion, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Search button
        Button(
            onClick = { scope.launch { performSearch(searchQuery) } },
            modifier = Modifier.fillMaxWidth(),
            enabled = searchQuery.isNotBlank() && !isSearching
        ) {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Searching via ${if (useTor) "Tor" else "Clearnet"}...")
            } else {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search via ${if (useTor) "Tor" else "Clearnet"}")
            }
        }
        
        // Network status indicator
        if (useTor && !torAvailable) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Tor network not available. Falling back to clearnet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content area
        when {
            showHistory && searchHistory.isNotEmpty() -> {
                Text(
                    text = "Search History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn {
                    items(searchHistory) { historyItem ->
                        SearchHistoryItem(
                            query = historyItem,
                            onClick = { 
                                searchQuery = historyItem.query
                                showHistory = false
                            }
                        )
                    }
                }
            }
            
            searchResults.isNotEmpty() -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${searchResults.size} results found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    AssistChip(
                        onClick = { },
                        label = { 
                            Text(
                                if (useTor) "🔒 Tor" else "🌐 Clearnet",
                                style = MaterialTheme.typography.labelSmall
                            ) 
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn {
                    items(searchResults) { result ->
                        SearchResultItem(result = result)
                    }
                }
            }
            
            else -> {
                // Empty state
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            if (useTor) Icons.Default.Security else Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (useTor) "Anonymous Search Ready" else "Web Search Ready",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (useTor) 
                                "Search the web anonymously through the Tor network" 
                            else 
                                "Search the web directly for fast results",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(result: SearchResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { /* TODO: Open URL */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and source indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (result.source == SearchSource.TOR) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = "Tor",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // URL
            Text(
                text = result.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Snippet
            Text(
                text = result.snippet,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Timestamp
            Text(
                text = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()).format(Date(result.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun SearchHistoryItem(query: SearchHistoryEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = query.query,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${query.resultCount} results • ${query.source} • ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(query.timestamp))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Icon(
                Icons.Default.NorthWest,
                contentDescription = "Use query",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private fun generateMockSearchResults(query: String, source: SearchSource): List<SearchResult> {
    val baseResults = listOf(
        SearchResult(
            id = "1",
            title = "Understanding $query - Complete Guide",
            url = "https://example.com/guide-to-${query.lowercase().replace(" ", "-")}",
            snippet = "This comprehensive guide covers everything you need to know about $query, including detailed explanations, examples, and best practices for implementation.",
            timestamp = System.currentTimeMillis(),
            source = source
        ),
        SearchResult(
            id = "2",
            title = "$query: Latest News and Updates",
            url = "https://news.example.com/${query.lowercase().replace(" ", "-")}-updates",
            snippet = "Stay up to date with the latest developments in $query. Recent research and industry insights provide new perspectives on this important topic.",
            timestamp = System.currentTimeMillis(),
            source = source
        ),
        SearchResult(
            id = "3",
            title = "How to Get Started with $query",
            url = "https://tutorial.example.com/getting-started-${query.lowercase().replace(" ", "-")}",
            snippet = "A beginner-friendly introduction to $query with step-by-step instructions, practical examples, and common troubleshooting tips.",
            timestamp = System.currentTimeMillis(),
            source = source
        ),
        SearchResult(
            id = "4",
            title = "$query Research and Analysis",
            url = "https://research.example.com/analysis-of-${query.lowercase().replace(" ", "-")}",
            snippet = "In-depth research analysis of $query trends, statistical data, and expert opinions from leading professionals in the field.",
            timestamp = System.currentTimeMillis(),
            source = source
        ),
        SearchResult(
            id = "5",
            title = "Advanced $query Techniques",
            url = "https://advanced.example.com/${query.lowercase().replace(" ", "-")}-techniques",
            snippet = "Explore advanced techniques and methodologies related to $query. Professional insights and cutting-edge approaches for experienced practitioners.",
            timestamp = System.currentTimeMillis(),
            source = source
        )
    )

    // Return fewer results for Tor to simulate slower/limited results
    return if (source == SearchSource.TOR) {
        baseResults.take(3)
    } else {
        baseResults
    }
}
