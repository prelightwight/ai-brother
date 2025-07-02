package com.prelightwight.aibrother.files

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

data class DocumentFile(
    val uri: Uri,
    val name: String,
    val size: Long,
    val mimeType: String?,
    val dateAdded: Long = System.currentTimeMillis()
)

data class ExtractedContent(
    val fileName: String,
    val content: String,
    val wordCount: Int,
    val extractedAt: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScreen() {
    var documents by remember { mutableStateOf(listOf<DocumentFile>()) }
    var extractedContents by remember { mutableStateOf(listOf<ExtractedContent>()) }
    var searchQuery by remember { mutableStateOf("") }
    var isExtracting by remember { mutableStateOf(false) }
    var selectedDocument by remember { mutableStateOf<DocumentFile?>(null) }
    var showContent by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                coroutineScope.launch {
                    val newDocuments = uris.mapNotNull { uri ->
                        getDocumentInfo(context, uri)
                    }
                    documents = documents + newDocuments
                }
            }
        }
    )
    
    fun extractTextFromDocument(document: DocumentFile) {
        coroutineScope.launch {
            isExtracting = true
            try {
                val content = extractTextContent(context, document.uri, document.mimeType)
                if (content.isNotBlank()) {
                    val extracted = ExtractedContent(
                        fileName = document.name,
                        content = content,
                        wordCount = content.split("\\s+".toRegex()).size
                    )
                    extractedContents = extractedContents + extracted
                }
            } catch (e: Exception) {
                // Handle extraction error
            } finally {
                isExtracting = false
            }
        }
    }
    
    fun removeDocument(document: DocumentFile) {
        documents = documents.filter { it.uri != document.uri }
        extractedContents = extractedContents.filter { it.fileName != document.name }
    }
    
    val filteredDocuments = if (searchQuery.isBlank()) {
        documents
    } else {
        documents.filter { 
            it.name.contains(searchQuery, ignoreCase = true) 
        }
    }
    
    val filteredContents = if (searchQuery.isBlank()) {
        extractedContents
    } else {
        extractedContents.filter { 
            it.fileName.contains(searchQuery, ignoreCase = true) || 
            it.content.contains(searchQuery, ignoreCase = true)
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
                text = "📂 File Library",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                if (documents.isNotEmpty()) {
                    IconButton(onClick = { 
                        documents = emptyList()
                        extractedContents = emptyList()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear All")
                    }
                }
                FilledTonalButton(
                    onClick = {
                        filePickerLauncher.launch(arrayOf(
                            "text/*", 
                            "application/pdf", 
                            "application/msword",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        ))
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Files")
                }
            }
        }

        // Search Bar
        if (documents.isNotEmpty() || extractedContents.isNotEmpty()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search files and content...") },
                leadingIcon = { 
                    Icon(Icons.Default.Search, contentDescription = "Search") 
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Statistics Cards
        if (documents.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${documents.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Files",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${extractedContents.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Extracted",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val totalWords = extractedContents.sumOf { it.wordCount }
                        Text(
                            text = if (totalWords > 1000) "${totalWords/1000}K" else "$totalWords",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Words",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        // Content Area
        if (documents.isEmpty() && extractedContents.isEmpty()) {
            // Empty State
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
                        Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No files yet",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Upload documents to extract and search through their content. Supports PDF, Word docs, and text files.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Documents Section
                if (filteredDocuments.isNotEmpty()) {
                    item {
                        Text(
                            text = "📄 Documents (${filteredDocuments.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                items(filteredDocuments) { document ->
                    DocumentCard(
                        document = document,
                        isExtracting = isExtracting,
                        hasExtracted = extractedContents.any { it.fileName == document.name },
                        onExtract = { extractTextFromDocument(document) },
                        onRemove = { removeDocument(document) },
                        onView = { 
                            selectedDocument = document
                            showContent = true
                        }
                    )
                }
                
                // Extracted Content Section
                if (filteredContents.isNotEmpty()) {
                    item {
                        Text(
                            text = "📝 Extracted Content (${filteredContents.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                items(filteredContents) { content ->
                    ExtractedContentCard(
                        content = content,
                        searchQuery = searchQuery,
                        onClick = { 
                            // Show full content
                        }
                    )
                }
            }
        }
    }
    
    // Content Viewer Dialog
    if (showContent && selectedDocument != null) {
        AlertDialog(
            onDismissRequest = { showContent = false },
            title = { Text(selectedDocument!!.name) },
            text = {
                val content = extractedContents.find { it.fileName == selectedDocument!!.name }
                if (content != null) {
                    LazyColumn(
                        modifier = Modifier.height(300.dp)
                    ) {
                        item {
                            Text(content.content)
                        }
                    }
                } else {
                    Text("Content not extracted yet. Please extract the text first.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showContent = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun DocumentCard(
    document: DocumentFile,
    isExtracting: Boolean,
    hasExtracted: Boolean,
    onExtract: () -> Unit,
    onRemove: () -> Unit,
    onView: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onView
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = document.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${formatFileSize(document.size)} • ${dateFormat.format(Date(document.dateAdded))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (hasExtracted) {
                            Text(
                                text = "✅ Content extracted",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Row {
                    if (!hasExtracted) {
                        if (isExtracting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            TextButton(onClick = onExtract) {
                                Text("Extract")
                            }
                        }
                    }
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Clear, contentDescription = "Remove")
                    }
                }
            }
        }
    }
}

@Composable
fun ExtractedContentCard(
    content: ExtractedContent,
    searchQuery: String,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = content.fileName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${content.wordCount} words • ${dateFormat.format(Date(content.extractedAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val preview = if (searchQuery.isNotBlank() && content.content.contains(searchQuery, ignoreCase = true)) {
                        val index = content.content.indexOf(searchQuery, ignoreCase = true)
                        val start = maxOf(0, index - 50)
                        val end = minOf(content.content.length, index + searchQuery.length + 50)
                        "..." + content.content.substring(start, end) + "..."
                    } else {
                        content.content.take(150) + if (content.content.length > 150) "..." else ""
                    }
                    
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// Helper Functions
private fun getDocumentInfo(context: Context, uri: Uri): DocumentFile? {
    return try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                
                val name = if (nameIndex != -1) it.getString(nameIndex) else "Unknown"
                val size = if (sizeIndex != -1) it.getLong(sizeIndex) else 0
                val mimeType = context.contentResolver.getType(uri)
                
                DocumentFile(uri, name ?: "Unknown", size, mimeType)
            } else null
        }
    } catch (e: Exception) {
        null
    }
}

private fun extractTextContent(context: Context, uri: Uri, mimeType: String?): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        inputStream?.use { stream ->
            when {
                mimeType?.startsWith("text/") == true -> {
                    BufferedReader(InputStreamReader(stream)).use { reader ->
                        reader.readText()
                    }
                }
                else -> {
                    // For other file types, try to read as text
                    // In a full implementation, you'd use libraries like Apache Tika
                    BufferedReader(InputStreamReader(stream)).use { reader ->
                        reader.readText()
                    }
                }
            }
        } ?: ""
    } catch (e: Exception) {
        ""
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        bytes >= 1024 -> "${bytes / 1024} KB"
        else -> "$bytes B"
    }
}
