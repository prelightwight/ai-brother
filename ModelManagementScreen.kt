package com.prelightwight.aibrother.models

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManagementScreen(
    onModelSelected: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val modelDownloader = remember { ModelDownloader(context) }
    
    var availableModels by remember { mutableStateOf(modelDownloader.getAvailableModels()) }
    var downloadedModels by remember { mutableStateOf(modelDownloader.getDownloadedModels()) }
    var downloadProgress by remember { mutableStateOf<Map<String, DownloadProgress>>(emptyMap()) }
    var selectedTab by remember { mutableStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf<ModelInfo?>(null) }
    var storageInfo by remember { mutableStateOf(modelDownloader.getModelStorageInfo()) }

    // Refresh data
    LaunchedEffect(Unit) {
        downloadedModels = modelDownloader.getDownloadedModels()
        storageInfo = modelDownloader.getModelStorageInfo()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Model Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Storage Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Storage Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val totalSpace = storageInfo["totalSpace"] as Long
                val freeSpace = storageInfo["freeSpace"] as Long
                val downloadedCount = storageInfo["downloadedModels"] as Int
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Downloaded Models: $downloadedCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Free Space: ${formatBytes(freeSpace)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Tab Selection
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Available Models") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Downloaded") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content based on selected tab
        when (selectedTab) {
            0 -> AvailableModelsTab(
                models = availableModels,
                downloadProgress = downloadProgress,
                downloadedModels = downloadedModels,
                onDownload = { modelId ->
                    coroutineScope.launch {
                        modelDownloader.downloadModel(modelId).collect { progress ->
                            downloadProgress = downloadProgress + (modelId to progress)
                            if (progress.status == DownloadStatus.COMPLETED) {
                                downloadedModels = modelDownloader.getDownloadedModels()
                                storageInfo = modelDownloader.getModelStorageInfo()
                            }
                        }
                    }
                }
            )
            1 -> DownloadedModelsTab(
                models = downloadedModels,
                onSelectModel = onModelSelected,
                onDeleteModel = { model ->
                    showDeleteDialog = model
                }
            )
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { model ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Model") },
            text = {
                Text("Are you sure you want to delete ${model.name}? This will free up ${model.size} of storage.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            modelDownloader.deleteModel(model.id)
                            downloadedModels = modelDownloader.getDownloadedModels()
                            storageInfo = modelDownloader.getModelStorageInfo()
                        }
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AvailableModelsTab(
    models: List<ModelInfo>,
    downloadProgress: Map<String, DownloadProgress>,
    downloadedModels: List<ModelInfo>,
    onDownload: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(models) { model ->
            ModelCard(
                model = model,
                isDownloaded = downloadedModels.any { it.id == model.id },
                downloadProgress = downloadProgress[model.id],
                onAction = { onDownload(model.id) }
            )
        }
    }
}

@Composable
private fun DownloadedModelsTab(
    models: List<ModelInfo>,
    onSelectModel: (String) -> Unit,
    onDeleteModel: (ModelInfo) -> Unit
) {
    if (models.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No models downloaded",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Download models from the Available Models tab",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(models) { model ->
                DownloadedModelCard(
                    model = model,
                    onSelect = { onSelectModel(model.id) },
                    onDelete = { onDeleteModel(model) }
                )
            }
        }
    }
}

@Composable
private fun ModelCard(
    model: ModelInfo,
    isDownloaded: Boolean,
    downloadProgress: DownloadProgress?,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        text = model.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${model.parameters} • ${model.quantization} • ${model.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                when {
                    isDownloaded -> {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Downloaded",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    downloadProgress?.status == DownloadStatus.DOWNLOADING -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                    else -> {
                        FilledTonalButton(onClick = onAction) {
                            Text("Download")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = model.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Use case: ${model.useCase}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Context: ${model.contextLength}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Download progress
            downloadProgress?.let { progress ->
                if (progress.status == DownloadStatus.DOWNLOADING) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress.percentage / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${progress.percentage}% • ${formatBytes(progress.bytesDownloaded)} / ${formatBytes(progress.totalBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadedModelCard(
    model: ModelInfo,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${model.parameters} • ${model.quantization} • ${model.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = model.useCase,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Row {
                IconButton(onClick = onSelect) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Select Model",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Model",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
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