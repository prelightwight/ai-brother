package com.prelightwight.aibrother.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.prelightwight.aibrother.ui.screens.ChatScreen
import com.prelightwight.aibrother.ui.screens.BrainScreen
import com.prelightwight.aibrother.ui.screens.SettingsScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Chat.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Chat.route) {
                ChatScreen()
            }
            composable(Screen.Brain.route) {
                BrainScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String) {
    object Chat : Screen("chat", "Chat")
    object Brain : Screen("brain", "Brain")
    object Settings : Screen("settings", "Settings")
}