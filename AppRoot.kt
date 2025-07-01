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
import com.prelightwight.aibrother.tutorial.TutorialScreen
import com.prelightwight.aibrother.tutorial.WithTutorial

@Composable
fun AppRoot() {
    val navController = rememberNavController()

    WithTutorial(screen = TutorialScreen.WELCOME) {
        Scaffold(
            bottomBar = { BottomNavBar(navController) }
        ) { paddingValues ->
            NavGraph(navController = navController, padding = paddingValues)
        }
    }
}

