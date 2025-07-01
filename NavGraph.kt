package com.prelightwight.aibrother.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.prelightwight.aibrother.brain.BrainScreen
import com.prelightwight.aibrother.chat.ChatScreen
import com.prelightwight.aibrother.files.FileScreen
import com.prelightwight.aibrother.images.ImageScreen
import com.prelightwight.aibrother.knowledge.KnowledgeScreen
import com.prelightwight.aibrother.llm.LLMManagementScreen
import com.prelightwight.aibrother.search.SearchScreen
import com.prelightwight.aibrother.settings.SettingsScreen
import com.prelightwight.aibrother.tutorial.TutorialScreen
import com.prelightwight.aibrother.tutorial.WithTutorial

@Composable
fun NavGraph(navController: NavHostController, padding: PaddingValues) {
    NavHost(navController = navController, startDestination = Screen.Chat.route) {
        composable(Screen.Chat.route) {
            WithTutorial(screen = TutorialScreen.CHAT) {
                ChatScreen(
                    onNavigateToLLMManagement = {
                        navController.navigate(Screen.LLMManagement.route)
                    }
                )
            }
        }
        composable(Screen.Brain.route) {
            WithTutorial(screen = TutorialScreen.BRAIN) {
                BrainScreen()
            }
        }
        composable(Screen.Files.route) {
            WithTutorial(screen = TutorialScreen.FILES) {
                FileScreen()
            }
        }
        composable(Screen.Knowledge.route) {
            WithTutorial(screen = TutorialScreen.KNOWLEDGE) {
                KnowledgeScreen()
            }
        }
        composable(Screen.Search.route) {
            WithTutorial(screen = TutorialScreen.SEARCH) {
                SearchScreen()
            }
        }
        composable(Screen.Images.route) {
            WithTutorial(screen = TutorialScreen.IMAGES) {
                ImageScreen()
            }
        }
        composable(Screen.LLMManagement.route) {
            LLMManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            WithTutorial(screen = TutorialScreen.SETTINGS) {
                SettingsScreen()
            }
        }
    }
}
