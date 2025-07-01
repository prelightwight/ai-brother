package com.prelightwight.aibrother.llm

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LLMManagementScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: LLMManagementViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val state by viewModel.state.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showAddModelDialog by remember { mutableStateOf(false) }
    var showModelDetails by remember { mutableStateOf<LLMModel?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<LocalModel?>(null) }

    // File picker for local models
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    val filename = getFilenameFromUri(context, uri) ?: "custom_model"
                    viewModel.addLocalModel(uri, filename)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    "🤖 LLM Management",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { showAddModelDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Model")
                }
            }
        )

        when (val currentState = state) {
            is LLMManagerState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Initializing LLM Manager...")
                    }
                }
            }
            
            is LLMManagerState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Error: ${currentState.message}",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.initialize() }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
            
            is LLMManagerState.Ready -> {
                // Tab Row
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Available Models") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Local Models (${currentState.localModels.size})") }
                    )
                }

                // Tab Content
                when (selectedTab) {
                    0 -> AvailableModelsTab(
                        models = currentState.availableModels,
                        downloadProgress = downloadProgress,
                        onDownloadModel = { model ->
                            scope.launch {
                                viewModel.downloadModel(model)
                            }
                        },
                        onShowDetails = { model ->
                            showModelDetails = model
                        },
                        onCancelDownload = { modelId ->
                            scope.launch {
                                viewModel.cancelDownload(modelId)
                            }
                        }
                    )
                    
                    1 -> LocalModelsTab(
                        models = currentState.localModels,
                        activeModel = currentState.activeModel,
                        onSelectModel = { model ->
                            scope.launch {
                                viewModel.setActiveModel(model)
                            }
                        },
                        onDeleteModel = { model ->
                            showDeleteConfirmation = model
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showAddModelDialog) {
        AddModelDialog(
            onDismiss = { showAddModelDialog = false },
            onAddFromFile = {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("*/*"))
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                filePickerLauncher.launch(intent)
                showAddModelDialog = false
            }
        )
    }

    showModelDetails?.let { model ->
        ModelDetailsDialog(
            model = model,
            isDownloaded = viewModel.isModelDownloaded(model),
            downloadProgress = downloadProgress[model.id],
            onDismiss = { showModelDetails = null },
            onDownload = {
                scope.launch {
                    viewModel.downloadModel(model)
                }
            },
            onCancelDownload = {
                scope.launch {
                    viewModel.cancelDownload(model.id)
                }
            }
        )
    }

    showDeleteConfirmation?.let { model ->
        DeleteConfirmationDialog(
            model = model,
            onDismiss = { showDeleteConfirmation = null },
            onConfirm = {
                scope.launch {
                    viewModel.deleteModel(model)
                }
                showDeleteConfirmation = null
            }
        )
    }
}

@Composable
private fun AvailableModelsTab(
    models: List<LLMModel>,
    downloadProgress: Map<String, DownloadProgress>,
    onDownloadModel: (LLMModel) -> Unit,
    onShowDetails: (LLMModel) -> Unit,
    onCancelDownload: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    
    val categories = listOf("All", "Recommended", "Beginner Friendly", "Coding", "Creative Writing", "Compact")
    
    val filteredModels = when (selectedCategory) {
        "All" -> models
        "Recommended" -> models.filter { it.isRecommended }
        else -> models.filter { model ->
            model.tags.any { it.equals(selectedCategory, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Category Filter
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    selected = selectedCategory == category
                )
            }
        }

        // Models List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredModels) { model ->
                AvailableModelCard(
                    model = model,
                    downloadProgress = downloadProgress[model.id],
                    onDownload = { onDownloadModel(model) },
                    onShowDetails = { onShowDetails(model) },
                    onCancelDownload = { onCancelDownload(model.id) }
                )
            }
        }
    }
}

@Composable
private fun LocalModelsTab(
    models: List<LocalModel>,
    activeModel: LocalModel?,
    onSelectModel: (LocalModel) -> Unit,
    onDeleteModel: (LocalModel) -> Unit
) {
    if (models.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Storage,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "No Local Models",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Download models from the Available Models tab or add your own files",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(models) { model ->
                LocalModelCard(
                    model = model,
                    isActive = activeModel?.id == model.id,
                    onSelect = { onSelectModel(model) },
                    onDelete = { onDeleteModel(model) }
                )
            }
        }
    }
}

@Composable
private fun AvailableModelCard(
    model: LLMModel,
    downloadProgress: DownloadProgress?,
    onDownload: () -> Unit,
    onShowDetails: () -> Unit,
    onCancelDownload: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onShowDetails() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = model.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (model.isRecommended) {
                            AssistChip(
                                onClick = { },
                                label = { Text("Recommended") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = model.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoChip(label = model.parameters, icon = Icons.Default.Memory)
                        InfoChip(label = model.size, icon = Icons.Default.Storage)
                        InfoChip(label = model.quantization, icon = Icons.Default.Compress)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Tags
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(model.tags) { tag ->
                    SuggestionChip(
                        onClick = { },
                        label = { 
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Download Section
            when {
                downloadProgress?.isDownloading == true -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Downloading... ${(downloadProgress.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = downloadProgress.speed,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        LinearProgressIndicator(
                            progress = downloadProgress.progress,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${formatBytes(downloadProgress.downloadedBytes)} / ${formatBytes(downloadProgress.totalBytes)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(
                                onClick = onCancelDownload
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
                
                downloadProgress?.isCompleted == true -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Downloaded",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                downloadProgress?.error != null -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Error: ${downloadProgress.error}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        TextButton(onClick = onDownload) {
                            Text("Retry")
                        }
                    }
                }
                
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Author: ${model.author}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Button(
                            onClick = onDownload
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalModelCard(
    model: LocalModel,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = model.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isActive) {
                            AssistChip(
                                onClick = { },
                                label = { Text("Active") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    labelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                        if (model.isFromDownload) {
                            AssistChip(
                                onClick = { },
                                label = { Text("Downloaded") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoChip(
                            label = formatFileSize(model.size),
                            icon = Icons.Default.Storage
                        )
                        InfoChip(
                            label = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(model.dateAdded),
                            icon = Icons.Default.CalendarToday
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = model.filepath,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isActive) {
                    Button(
                        onClick = onSelect,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Activate")
                    }
                }
                
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    if (!isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddModelDialog(
    onDismiss: () -> Unit,
    onAddFromFile: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add LLM Model",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Add your own GGUF model files or download from our curated collection",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddFromFile,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Browse Local Files")
                    }
                    
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
                
                Divider()
                
                Text(
                    text = "Supported formats: .gguf, .bin",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ModelDetailsDialog(
    model: LLMModel,
    isDownloaded: Boolean,
    downloadProgress: DownloadProgress?,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = model.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Model specifications
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow("Parameters", model.parameters)
                    DetailRow("Size", model.size)
                    DetailRow("Quantization", model.quantization)
                    DetailRow("Author", model.author)
                    DetailRow("License", model.license)
                }
                
                // Tags
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(model.tags) { tag ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(tag) }
                        )
                    }
                }
                
                Divider()
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                    
                    when {
                        downloadProgress?.isDownloading == true -> {
                            Button(
                                onClick = onCancelDownload,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel Download")
                            }
                        }
                        isDownloaded -> {
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                enabled = false
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Downloaded")
                            }
                        }
                        else -> {
                            Button(
                                onClick = onDownload,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Download")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    model: LocalModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Model") },
        text = { 
            Text("Are you sure you want to delete \"${model.name}\"? This action cannot be undone.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> "${(bytes / 1_000_000_000.0).let { "%.1f".format(it) }} GB"
        bytes >= 1_000_000 -> "${(bytes / 1_000_000.0).let { "%.1f".format(it) }} MB"
        bytes >= 1_000 -> "${(bytes / 1_000.0).let { "%.1f".format(it) }} KB"
        else -> "$bytes B"
    }
}

private fun formatFileSize(bytes: Long): String = formatBytes(bytes)

private fun getFilenameFromUri(context: android.content.Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) cursor.getString(nameIndex) else null
            } else null
        }
    } catch (e: Exception) {
        null
    }
}