package com.prelightwight.aibrother.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun ChatScreen() {
    var messages by remember { mutableStateOf(listOf<String>()) }
    var currentInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    var selectedModelUri by remember { mutableStateOf<Uri?>(null) }

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

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            messages.forEach { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth()
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = currentInput,
                onValueChange = { currentInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your message...") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (currentInput.isNotBlank()) {
                            messages = messages + "You: $currentInput"
                            // Simulated response (stub)
                            messages = messages + "AI: ${currentInput.reversed()}"
                            currentInput = ""
                            keyboardController?.hide()
                        }
                    }
                ),
                maxLines = 4
            )
            IconButton(
                onClick = {
                    if (currentInput.isNotBlank()) {
                        messages = messages + "You: $currentInput"
                        // Simulated response (stub)
                        messages = messages + "AI: ${currentInput.reversed()}"
                        currentInput = ""
                        keyboardController?.hide()
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}
