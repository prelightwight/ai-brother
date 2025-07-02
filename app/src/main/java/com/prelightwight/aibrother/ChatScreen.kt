package com.prelightwight.aibrother.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.prelightwight.aibrother.llm.LlamaRunner
import com.prelightwight.aibrother.llm.InferenceConfig
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val id: String = UUID.randomUUID().toString(),
    val isError: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    selectedModelId: String? = null
) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var currentInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var modelStatus by remember { mutableStateOf("No model selected") }
    var selectedModelUri by remember { mutableStateOf<Uri?>(null) }
    var showModelInfo by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var inferenceConfig by remember { mutableStateOf(InferenceConfig()) }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Initialize LlamaRunner
    LaunchedEffect(context) {
        LlamaRunner.init(context)
    }

    // Load model if selectedModelId is provided
    LaunchedEffect(selectedModelId) {
        if (selectedModelId != null) {
            modelStatus = "Loading model: $selectedModelId..."
            try {
                val result = LlamaRunner.loadModelById(selectedModelId)
                modelStatus = if (result.startsWith("Error:")) {
                    result
                } else {
                    "✅ $result"
                }
                performanceStats = LlamaRunner.getPerformanceStats()
            } catch (e: Exception) {
                modelStatus = "Error loading model: ${e.message}"
            }
        }
    }

    // Monitor performance stats
    var performanceStats by remember { mutableStateOf(mapOf<String, Any>()) }
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            performanceStats = LlamaRunner.getPerformanceStats()
        }
    }

    val modelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                selectedModelUri = uri
                LlamaRunner.setModelUri(uri)
                val fileName = uri.lastPathSegment ?: "Unknown"
                modelStatus = "Model selected: $fileName"
                
                // Auto-load the model
                coroutineScope.launch {
                    modelStatus = "Loading model..."
                    try {
                        val result = LlamaRunner.loadModel()
                        modelStatus = if (result.startsWith("Error:")) {
                            result
                        } else {
                            "✅ $result"
                        }
                        performanceStats = LlamaRunner.getPerformanceStats()
                    } catch (e: Exception) {
                        modelStatus = "Error: ${e.message}"
                    }
                }
            }
        }
    )

    fun sendMessage() {
        if (currentInput.isBlank() || isLoading) return
        
        val userMessage = ChatMessage(content = currentInput, isUser = true)
        messages = messages + userMessage
        val prompt = currentInput
        currentInput = ""
        keyboardController?.hide()
        
        coroutineScope.launch {
            isLoading = true
            try {
                val response = LlamaRunner.infer(prompt, inferenceConfig)
                val aiMessage = ChatMessage(
                    content = response, 
                    isUser = false,
                    isError = response.startsWith("Error:")
                )
                messages = messages + aiMessage
                
                // Update performance stats
                performanceStats = LlamaRunner.getPerformanceStats()
                
                // Auto-scroll to bottom
                listState.animateScrollToItem(messages.size - 1)
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    content = "System Error: ${e.message}", 
                    isUser = false,
                    isError = true
                )
                messages = messages + errorMessage
            } finally {
                isLoading = false
            }
        }
    }

    fun clearConversation() {
        messages = emptyList()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // Header with actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🤖 AI Brother Chat",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Row {
                if (messages.isNotEmpty()) {
                    IconButton(onClick = { clearConversation() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Chat")
                    }
                }
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        }

        // Model selection and status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        modelPickerLauncher.launch(arrayOf("*/*"))
                    }) {
                        Text("🧠 Load Model")
                    }
                    
                    if (LlamaRunner.isModelLoaded()) {
                        IconButton(onClick = { showModelInfo = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Model Info")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = modelStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        modelStatus.startsWith("Error:") -> MaterialTheme.colorScheme.error
                        modelStatus.startsWith("✅") -> MaterialTheme.colorScheme.primary
                        modelStatus.contains("Loading") -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Performance stats (if available)
                if (performanceStats.isNotEmpty() && LlamaRunner.isModelLoaded()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val tokensPerSec = performanceStats["averageTokensPerSecond"] as? Float ?: 0f
                    val lastInferenceTime = performanceStats["lastInferenceTimeMs"] as? Long ?: 0L
                    
                    if (tokensPerSec > 0f || lastInferenceTime > 0L) {
                        Text(
                            text = "⚡ ${String.format("%.1f", tokensPerSec)} tokens/sec (${lastInferenceTime}ms)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // Chat messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "👋 Welcome to AI Brother!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your privacy-focused AI assistant. Start by loading a model, then ask me anything!",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            items(messages) { message ->
                ChatMessageBubble(message = message)
            }
            
            if (isLoading) {
                item {
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
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI is thinking...")
                        }
                    }
                }
            }
        }

        // Input area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                TextField(
                    value = currentInput,
                    onValueChange = { currentInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { 
                        Text(
                            if (LlamaRunner.isModelLoaded()) 
                                "Type your message..." 
                            else 
                                "Load a model first..."
                        ) 
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = { sendMessage() }
                    ),
                    maxLines = 4,
                    enabled = !isLoading && LlamaRunner.isModelLoaded()
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                FilledIconButton(
                    onClick = { sendMessage() },
                    enabled = currentInput.isNotBlank() && !isLoading && LlamaRunner.isModelLoaded()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }

    // Model Info Dialog
    if (showModelInfo) {
        val modelInfo = LlamaRunner.getModelInfo()
        AlertDialog(
            onDismissRequest = { showModelInfo = false },
            title = { Text("Model Information") },
            text = {
                if (modelInfo != null) {
                    Column {
                        Text("Name: ${modelInfo.name}")
                        Text("Size: ${modelInfo.size / 1024 / 1024} MB")
                        Text("Context: ${modelInfo.contextSize}")
                        Text("Vocab: ${modelInfo.vocabSize}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Status: Loaded", color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Text("No model information available")
                }
            },
            confirmButton = {
                TextButton(onClick = { showModelInfo = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Settings Dialog
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Chat Settings") },
            text = {
                Column {
                    Text("Max Tokens: ${inferenceConfig.maxTokens}")
                    Slider(
                        value = inferenceConfig.maxTokens.toFloat(),
                        onValueChange = { 
                            inferenceConfig = inferenceConfig.copy(maxTokens = it.toInt())
                        },
                        valueRange = 128f..1024f,
                        steps = 7
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Temperature: ${"%.2f".format(inferenceConfig.temperature)}")
                    Slider(
                        value = inferenceConfig.temperature,
                        onValueChange = { 
                            inferenceConfig = inferenceConfig.copy(temperature = it)
                        },
                        valueRange = 0.1f..1.0f
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    inferenceConfig = InferenceConfig() // Reset to defaults
                    showSettings = false 
                }) {
                    Text("Reset")
                }
            }
        )
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    message.isError -> MaterialTheme.colorScheme.errorContainer
                    message.isUser -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            message.isError -> "⚠️ Error"
                            message.isUser -> "You"
                            else -> "AI Brother"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            message.isError -> MaterialTheme.colorScheme.onErrorContainer
                            message.isUser -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                    
                    Text(
                        text = timeFormat.format(Date(message.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            message.isError -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            message.isUser -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        message.isError -> MaterialTheme.colorScheme.onErrorContainer
                        message.isUser -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}
