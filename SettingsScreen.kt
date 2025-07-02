package com.prelightwight.aibrother.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var showModelSettings by remember { mutableStateOf(false) }
    var showPrivacySettings by remember { mutableStateOf(false) }
    var showAppInfo by remember { mutableStateOf(false) }
    var showClearData by remember { mutableStateOf(false) }
    
    // Settings state
    var darkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var temperature by remember { mutableStateOf(0.7f) }
    var maxTokens by remember { mutableStateOf(512) }
    var topK by remember { mutableStateOf(40) }
    var topP by remember { mutableStateOf(0.9f) }
    var streamingEnabled by remember { mutableStateOf(true) }
    var autoSaveConversations by remember { mutableStateOf(true) }
    var offlineMode by remember { mutableStateOf(true) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // AI Model Configuration
            item {
                SettingsSection(title = "🤖 AI Configuration") {
                    SettingsCard(
                        title = "Model Parameters",
                        subtitle = "Configure AI inference settings",
                        icon = Icons.Default.Psychology,
                        onClick = { showModelSettings = true }
                    )
                    
                    SettingsRow(
                        title = "Streaming Responses",
                        subtitle = "Get responses word by word",
                        icon = Icons.Default.Download,
                        checked = streamingEnabled,
                        onCheckedChange = { streamingEnabled = it }
                    )
                    
                    SettingsRow(
                        title = "Offline Mode",
                        subtitle = "Process everything locally",
                        icon = Icons.Default.Security,
                        checked = offlineMode,
                        onCheckedChange = { offlineMode = it }
                    )
                }
            }

            // App Preferences
            item {
                SettingsSection(title = "🎨 Appearance & Behavior") {
                    SettingsRow(
                        title = "Dark Mode",
                        subtitle = "Use dark theme",
                        icon = Icons.Default.Brightness4,
                        checked = darkMode,
                        onCheckedChange = { darkMode = it }
                    )
                    
                    SettingsRow(
                        title = "Notifications",
                        subtitle = "App notifications and alerts",
                        icon = Icons.Default.Notifications,
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                    
                    SettingsRow(
                        title = "Auto-save Conversations",
                        subtitle = "Automatically save chat history",
                        icon = Icons.Default.Storage,
                        checked = autoSaveConversations,
                        onCheckedChange = { autoSaveConversations = it }
                    )
                }
            }

            // Privacy & Security
            item {
                SettingsSection(title = "🔒 Privacy & Security") {
                    SettingsCard(
                        title = "Privacy Settings",
                        subtitle = "Data encryption and security options",
                        icon = Icons.Default.Security,
                        onClick = { showPrivacySettings = true }
                    )
                    
                    SettingsCard(
                        title = "Clear Data",
                        subtitle = "Delete conversations and cached files",
                        icon = Icons.Default.Delete,
                        onClick = { showClearData = true }
                    )
                }
            }

            // System Information
            item {
                SettingsSection(title = "📱 System") {
                    val runtime = Runtime.getRuntime()
                    val usedMemory = runtime.totalMemory() - runtime.freeMemory()
                    val maxMemory = runtime.maxMemory()
                    val memoryUsage = "${usedMemory / 1024 / 1024}MB / ${maxMemory / 1024 / 1024}MB"
                    
                    SettingsInfo(
                        title = "Memory Usage",
                        value = memoryUsage,
                        icon = Icons.Default.Memory
                    )
                    
                    SettingsInfo(
                        title = "Storage Used",
                        value = "~45MB", // Mock value
                        icon = Icons.Default.Storage
                    )
                    
                    SettingsCard(
                        title = "About AI Brother",
                        subtitle = "Version info and licenses",
                        icon = Icons.Default.Info,
                        onClick = { showAppInfo = true }
                    )
                }
            }

            // Developer Options
            item {
                SettingsSection(title = "⚙️ Advanced") {
                    SettingsInfo(
                        title = "Build Version",
                        value = "1.0.0 (Debug)",
                        icon = Icons.Default.Code
                    )
                    
                    SettingsInfo(
                        title = "Android Version",
                        value = android.os.Build.VERSION.RELEASE,
                        icon = Icons.Default.Android
                    )
                    
                    SettingsInfo(
                        title = "Model Status",
                        value = "Enhanced Mock Mode",
                        icon = Icons.Default.Tune
                    )
                }
            }
        }
    }

    // Model Settings Dialog
    if (showModelSettings) {
        AlertDialog(
            onDismissRequest = { showModelSettings = false },
            title = { Text("AI Model Parameters") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Temperature
                    Column {
                        Text(
                            text = "Temperature: ${String.format("%.1f", temperature)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Controls randomness (0.1 = focused, 1.0 = creative)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = temperature,
                            onValueChange = { temperature = it },
                            valueRange = 0.1f..1.0f,
                            steps = 8
                        )
                    }
                    
                    // Max Tokens
                    Column {
                        Text(
                            text = "Max Tokens: $maxTokens",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Maximum response length",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = maxTokens.toFloat(),
                            onValueChange = { maxTokens = it.toInt() },
                            valueRange = 128f..1024f,
                            steps = 7
                        )
                    }
                    
                    // Top K
                    Column {
                        Text(
                            text = "Top K: $topK",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Limits vocabulary to top K tokens",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = topK.toFloat(),
                            onValueChange = { topK = it.toInt() },
                            valueRange = 10f..100f,
                            steps = 8
                        )
                    }
                    
                    // Top P
                    Column {
                        Text(
                            text = "Top P: ${String.format("%.1f", topP)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Cumulative probability threshold",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = topP,
                            onValueChange = { topP = it },
                            valueRange = 0.1f..1.0f,
                            steps = 8
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModelSettings = false }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    // Reset to defaults
                    temperature = 0.7f
                    maxTokens = 512
                    topK = 40
                    topP = 0.9f
                    showModelSettings = false 
                }) {
                    Text("Reset")
                }
            }
        )
    }

    // Privacy Settings Dialog
    if (showPrivacySettings) {
        AlertDialog(
            onDismissRequest = { showPrivacySettings = false },
            title = { Text("Privacy & Security") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "🔒 Privacy by Design",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            listOf(
                                "All processing happens locally on your device",
                                "No data sent to external servers",
                                "Conversations stored encrypted",
                                "You control all your data"
                            ).forEach { feature ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✓",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = feature,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                    
                    Text(
                        text = "Your privacy is protected by design. AI Brother runs entirely offline and never shares your data with third parties.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacySettings = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Clear Data Dialog
    if (showClearData) {
        AlertDialog(
            onDismissRequest = { showClearData = false },
            title = { Text("Clear Data") },
            text = {
                Column {
                    Text(
                        text = "Choose what to clear:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    listOf(
                        "Chat conversations" to "Delete all chat history",
                        "Brain memories" to "Clear all stored memories",
                        "Cached files" to "Remove temporary files",
                        "App preferences" to "Reset all settings"
                    ).forEach { (title, description) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var checked by remember { mutableStateOf(false) }
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { checked = it }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        coroutineScope.launch {
                            // Perform cleanup operations
                        }
                        showClearData = false 
                    }
                ) {
                    Text("Clear Selected", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearData = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // App Info Dialog
    if (showAppInfo) {
        AlertDialog(
            onDismissRequest = { showAppInfo = false },
            title = { Text("About AI Brother") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🤖 AI Brother",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Version 1.0.0\nBuild 2024.07.02",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Your privacy-focused AI assistant that runs entirely on your device. Built with modern Android development practices and designed with privacy by default.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "🛠️ Built with:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            listOf(
                                "Kotlin & Jetpack Compose",
                                "Material 3 Design",
                                "Room Database",
                                "llama.cpp Integration",
                                "Modern Android Architecture"
                            ).forEach { tech ->
                                Text(
                                    text = "• $tech",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppInfo = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsInfo(
    title: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
