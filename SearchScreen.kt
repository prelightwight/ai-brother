package com.prelightwight.aibrother.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

data class SearchResult(
    val title: String,
    val url: String,
    val snippet: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class SearchEngine(val displayName: String, val baseUrl: String) {
    GOOGLE("Google", "https://www.google.com/search?q="),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q="),
    BING("Bing", "https://www.bing.com/search?q="),
    SEARX("SearX", "https://searx.be/search?q=")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<SearchResult>()) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedEngine by remember { mutableStateOf(SearchEngine.DUCKDUCKGO) }
    var useTor by remember { mutableStateOf(false) }
    var showEngineSelector by remember { mutableStateOf(false) }
    var searchHistory by remember { mutableStateOf(listOf<String>()) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun performSearch() {
        if (searchQuery.isBlank()) return
        
        coroutineScope.launch {
            isSearching = true
            try {
                // Add to search history
                searchHistory = listOf(searchQuery) + searchHistory.filter { it != searchQuery }.take(9)
                
                // Simulate web search (in a real implementation, you'd use WebView or HTTP client)
                searchResults = simulateWebSearch(searchQuery, selectedEngine)
            } finally {
                isSearching = false
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🌐 Web Search",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = { showEngineSelector = true }) {
                Icon(Icons.Default.Settings, contentDescription = "Search Settings")
            }
        }

        // Search Configuration Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (useTor) 
                    MaterialTheme.colorScheme.errorContainer 
                else 
                    MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedEngine.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (useTor) 
                                MaterialTheme.colorScheme.onErrorContainer 
                            else 
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (useTor) "🔒 Via Tor Network" else "🌐 Direct Connection",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (useTor) 
                                MaterialTheme.colorScheme.onErrorContainer 
                            else 
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Switch(
                        checked = useTor,
                        onCheckedChange = { useTor = it },
                        thumbContent = {
                            Icon(
                                if (useTor) Icons.Default.Shield else Icons.Default.Public,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                
                if (useTor) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ Tor mode provides enhanced privacy but may be slower. Ensure you have Orbot installed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Search the web...") },
            leadingIcon = { 
                Icon(Icons.Default.Search, contentDescription = "Search") 
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                } else {
                    IconButton(onClick = { performSearch() }) {
                        Icon(Icons.Default.Send, contentDescription = "Search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onSearch = { performSearch() }
            )
        )

        // Search History
        if (searchQuery.isEmpty() && searchHistory.isNotEmpty()) {
            Text(
                text = "Recent Searches",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(searchHistory) { query ->
                    Card(
                        onClick = { 
                            searchQuery = query
                            performSearch()
                        }
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
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = query,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { 
                                    searchHistory = searchHistory.filter { it != query }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Results
        if (isSearching) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Searching ${selectedEngine.displayName}${if (useTor) " via Tor" else ""}...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else if (searchResults.isNotEmpty()) {
            Text(
                text = "Search Results (${searchResults.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(searchResults) { result ->
                    SearchResultCard(result = result)
                }
            }
        } else if (searchQuery.isNotEmpty() && !isSearching) {
            // Empty results state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No results found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Try different search terms or check your connection",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Engine Selector Dialog
    if (showEngineSelector) {
        AlertDialog(
            onDismissRequest = { showEngineSelector = false },
            title = { Text("Search Engine") },
            text = {
                Column {
                    SearchEngine.values().forEach { engine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedEngine == engine,
                                onClick = { 
                                    selectedEngine = engine
                                    showEngineSelector = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = engine.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEngineSelector = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun SearchResultCard(result: SearchResult) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = result.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = result.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = result.snippet,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(result.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = { /* Open in browser */ }) {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = "Open",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun simulateWebSearch(query: String, engine: SearchEngine): List<SearchResult> {
    // This is a simulation - in a real implementation you'd use WebView or HTTP requests
    return listOf(
        SearchResult(
            title = "Understanding $query - Comprehensive Guide",
            url = "https://example.com/guide-to-${query.replace(" ", "-").lowercase()}",
            snippet = "Learn everything about $query with this comprehensive guide. Covering basics, advanced topics, and practical applications..."
        ),
        SearchResult(
            title = "$query: Latest Research and Findings",
            url = "https://research.example.com/${query.replace(" ", "-").lowercase()}",
            snippet = "Recent studies and research findings related to $query. Peer-reviewed articles and expert analysis..."
        ),
        SearchResult(
            title = "How to Get Started with $query",
            url = "https://tutorial.example.com/getting-started-${query.replace(" ", "-").lowercase()}",
            snippet = "Step-by-step tutorial for beginners looking to understand $query. Includes examples and best practices..."
        ),
        SearchResult(
            title = "$query Community Forum",
            url = "https://forum.example.com/topics/${query.replace(" ", "-").lowercase()}",
            snippet = "Join the discussion about $query. Ask questions, share experiences, and connect with experts..."
        ),
        SearchResult(
            title = "Tools and Resources for $query",
            url = "https://tools.example.com/${query.replace(" ", "-").lowercase()}",
            snippet = "Discover useful tools, resources, and utilities related to $query. Free and premium options available..."
        )
    )
}
