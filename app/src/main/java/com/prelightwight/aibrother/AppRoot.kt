package com.prelightwight.aibrother.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.*
import com.prelightwight.aibrother.ui.theme.AIBrotherTheme
import com.prelightwight.aibrother.brain.BrainScreen
import com.prelightwight.aibrother.chat.ChatScreen
import com.prelightwight.aibrother.files.FileScreen
import com.prelightwight.aibrother.knowledge.KnowledgeScreen
import com.prelightwight.aibrother.settings.SettingsScreen

@Composable
fun AppRoot() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        NavGraph(navController = navController, padding = paddingValues)
    }
}

