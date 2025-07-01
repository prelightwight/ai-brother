package com.prelightwight.aibrother.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            text = "⚙️ Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Use Dark Theme")
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = state.useDarkTheme,
                onCheckedChange = { viewModel.toggleDarkTheme() }
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Use Tor for Web Search")
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = state.useTor,
                onCheckedChange = { viewModel.toggleTor() }
            )
        }
    }
}
