package com.prelightwight.aibrother

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.prelightwight.aibrother.ui.theme.AIBrotherTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    AIBrotherTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("AI Brother") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🤖 AI Brother",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your privacy-focused AI assistant",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { /* TODO: Add navigation */ }
                ) {
                    Text("Get Started")
                }
            }
        }
    }
}

