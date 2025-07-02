package com.prelightwight.aibrother.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.prelightwight.aibrother.brain.BrainScreen
import com.prelightwight.aibrother.chat.ChatScreen
import com.prelightwight.aibrother.files.FileScreen
import com.prelightwight.aibrother.knowledge.KnowledgeScreen
import com.prelightwight.aibrother.settings.SettingsScreen
import com.prelightwight.aibrother.models.ModelManagementScreen

@Composable
fun NavGraph(navController: NavHostController, padding: PaddingValues) {
    NavHost(navController = navController, startDestination = Screen.Chat.route) {
        composable(Screen.Chat.route) {
            ChatScreen()
        }
        composable(Screen.Brain.route) {
            BrainScreen()
        }
        composable(Screen.Files.route) {
            FileScreen()
        }
        composable(Screen.Knowledge.route) {
            KnowledgeScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToModels = {
                    navController.navigate("models")
                }
            )
        }
        composable("models") {
            ModelManagementScreen(
                onModelSelected = { modelId ->
                    // Navigate back to chat and load selected model
                    navController.popBackStack()
                    navController.navigate("chat/${modelId}")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("chat/{modelId}") { backStackEntry ->
            val modelId = backStackEntry.arguments?.getString("modelId")
            ChatScreen(selectedModelId = modelId)
        }
    }
}
