package com.prelightwight.aibrother.chat

import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import com.prelightwight.aibrother.ai.AIModelManager
import com.prelightwight.aibrother.data.AppDatabase
import com.prelightwight.aibrother.data.ConversationEntity
import com.prelightwight.aibrother.data.MessageEntity
import com.prelightwight.aibrother.llm.LlamaRunner
import com.prelightwight.aibrother.voice.VoiceManager
import com.prelightwight.aibrother.voice.VoiceState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Permissions
    val microphonePermission = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)
    
    // AI and Voice managers
    val aiManager = remember { AIModelManager.getInstance(context) }
    val voiceManager = remember { VoiceManager.getInstance(context) }
    
    // Database
    val database = remember { AppDatabase.getDatabase(context) }
    
    // State
    var messages by remember { mutableStateOf(listOf<MessageEntity>()) }
    var currentInput by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var selectedModelUri by remember { mutableStateOf<Uri?>(null) }
    var currentConversationId by remember { mutableStateOf("default_conversation") }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Voice state
    val voiceState by voiceManager.voiceState.collectAsState()
    val recognitionResult by voiceManager.recognitionResults.collectAsState()
    val isListening by voiceManager.isListening.collectAsState()
    
    // Initialize managers
    LaunchedEffect(Unit) {
        aiManager.initialize()
        voiceManager.initialize()
        
        // Load conversation history
        database.messageDao().getMessagesForConversation(currentConversationId).collect { msgs ->
            messages = msgs
        }
    }
    
    // Handle voice recognition results
    LaunchedEffect(recognitionResult) {
        recognitionResult?.let { result ->
            if (!result.isPartial && result.text.isNotBlank()) {
                currentInput = result.text
                // Auto-send voice input
                sendMessage(result.text, aiManager, database, currentConversationId) { response ->
                    scope.launch {
                        voiceManager.speak(response)
                    }
                }
                currentInput = ""
            }
        }
    }
    
    suspend fun sendMessage(message: String, aiManager: AIModelManager, database: AppDatabase, conversationId: String, onResponse: ((String) -> Unit)? = null) {
        if (message.isBlank()) return
        
        isProcessing = true
        
        // Save user message
        val userMessage = MessageEntity(
            conversationId = conversationId,
            content = message,
            isFromUser = true
        )
        database.messageDao().insertMessage(userMessage)
        
        // Get AI response
        val aiResponse = try {
            val result = aiManager.generateChatResponse(message, messages.takeLast(5).map { it.content })
            result.text ?: "I'm having trouble processing your request right now."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
        
        // Save AI response
        val assistantMessage = MessageEntity(
            conversationId = conversationId,
            content = aiResponse,
            isFromUser = false
        )
        database.messageDao().insertMessage(assistantMessage)
        
        onResponse?.invoke(aiResponse)
        isProcessing = false
    }

    val modelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                selectedModelUri = uri
LlamaRunner.setModelUri(uri)
LlamaRunner.init(context)
            }
        }
    )

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            text = "AI Brother Chat",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(onClick = {
            modelPickerLauncher.launch(arrayOf("*/*")) // Optionally filter to .gguf
        }) {
            Text("🧠 Load Model File")
        }

        selectedModelUri?.let {
            Text(
                text = "Selected model: ${it.path}",
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                MessageBubble(
                    message = message,
                    onSpeak = { 
                        scope.launch { 
                            voiceManager.speak(message.content) 
                        } 
                    }
                )
            }
        }

        // Voice controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice input button
            PermissionRequired(
                permissionState = microphonePermission,
                permissionNotGrantedContent = {
                    Button(onClick = { microphonePermission.launchPermissionRequest() }) {
                        Text("Enable Voice")
                    }
                },
                permissionNotAvailableContent = {
                    Text("Voice not available")
                }
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            if (isListening) {
                                voiceManager.stopListening()
                            } else {
                                voiceManager.startListening()
                            }
                        }
                    },
                    enabled = voiceState != VoiceState.ERROR,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Listening" else "Start Voice Input"
                    )
                }
            }
            
            // Voice state indicator
            when (voiceState) {
                VoiceState.LISTENING -> Text("Listening...", color = MaterialTheme.colorScheme.primary)
                VoiceState.PROCESSING -> Text("Processing...", color = MaterialTheme.colorScheme.secondary)
                VoiceState.SPEAKING -> Text("Speaking...", color = MaterialTheme.colorScheme.tertiary)
                VoiceState.ERROR -> Text("Voice Error", color = MaterialTheme.colorScheme.error)
                else -> { /* No indicator */ }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = currentInput,
                onValueChange = { currentInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your message or use voice...") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        scope.launch {
                            sendMessage(currentInput, aiManager, database, currentConversationId)
                            currentInput = ""
                            keyboardController?.hide()
                        }
                    }
                ),
                maxLines = 4,
                enabled = !isProcessing
            )
            IconButton(
                onClick = {
                    scope.launch {
                        sendMessage(currentInput, aiManager, database, currentConversationId)
                        currentInput = ""
                        keyboardController?.hide()
                    }
                },
                enabled = currentInput.isNotBlank() && !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    onSpeak: () -> Unit
) {
    val isUser = message.isFromUser
    val backgroundColor = if (isUser) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.surfaceVariant
    
    val textColor = if (isUser) 
        MaterialTheme.colorScheme.onPrimary 
    else 
        MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // AI avatar
            Icon(
                Icons.Default.SmartToy,
                contentDescription = "AI",
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp)),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    
                    if (!isUser) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Speak",
                                modifier = Modifier.size(16.dp),
                                tint = textColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User avatar
            Icon(
                Icons.Default.Person,
                contentDescription = "User",
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp)),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
