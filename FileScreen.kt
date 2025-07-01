package com.prelightwight.aibrother.files

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

data class ProcessedFile(
    val name: String,
    val uri: Uri,
    val content: String,
    val summary: String,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScreen() {
    val context = LocalContext.current
    var selectedFiles by remember { mutableStateOf(listOf<ProcessedFile>()) }
    var isProcessing by remember { mutableStateOf(false) }
    var selectedFileContent by remember { mutableStateOf<ProcessedFile?>(null) }
    val scope = rememberCoroutineScope()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                scope.launch {
                    isProcessing = true
                    val processedFiles = uris.mapNotNull { uri ->
                        processFile(context, uri)
                    }
                    selectedFiles = selectedFiles + processedFiles
                    isProcessing = false
                }
            }
        }
    )

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            text = "📂 Files",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Action buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    filePicker.launch(arrayOf(
                        "text/plain", 
                        "application/pdf", 
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    ))
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Files")
            }

            Button(
                onClick = { selectedFiles = emptyList() },
                enabled = selectedFiles.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear All")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isProcessing) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Processing files...")
                }
            }
        }

        if (selectedFiles.isNotEmpty()) {
            Text(
                text = "Processed Files (${selectedFiles.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn {
                items(selectedFiles) { file ->
                    FileItem(
                        file = file,
                        onClick = { selectedFileContent = file }
                    )
                }
            }
        } else if (!isProcessing) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No files selected",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Upload documents to analyze and discuss with AI Brother",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // File content dialog
    selectedFileContent?.let { file ->
        AlertDialog(
            onDismissRequest = { selectedFileContent = null },
            title = { Text(file.name) },
            text = {
                LazyColumn {
                    item {
                        Text(
                            "Summary:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            file.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            "Content:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            file.content.take(1000) + if (file.content.length > 1000) "..." else "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedFileContent = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun FileItem(file: ProcessedFile, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = file.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Text(
                    text = "${file.content.length} characters",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private suspend fun processFile(context: Context, uri: Uri): ProcessedFile? {
    return try {
        val fileName = getFileName(context, uri) ?: "Unknown File"
        val content = readFileContent(context, uri)
        val summary = generateSummary(content)
        
        ProcessedFile(
            name = fileName,
            uri = uri,
            content = content,
            summary = summary,
            timestamp = System.currentTimeMillis()
        )
    } catch (e: Exception) {
        null
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    }
}

private fun readFileContent(context: Context, uri: Uri): String {
    return context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            reader.readText()
        }
    } ?: ""
}

private fun generateSummary(content: String): String {
    // Simple extractive summary - takes first few sentences
    val sentences = content.split(Regex("[.!?]+")).filter { it.trim().isNotEmpty() }
    return when {
        sentences.isEmpty() -> "Empty document"
        sentences.size <= 2 -> content.take(200)
        else -> sentences.take(2).joinToString(". ").take(200) + "..."
    }
}
