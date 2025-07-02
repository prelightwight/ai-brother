package com.prelightwight.aibrother.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Chat : Screen("chat", "Chat", Icons.Default.Chat)
    object Brain : Screen("brain", "Brain", Icons.Default.Memory)
    object Files : Screen("files", "Files", Icons.Default.Folder)
    object Knowledge : Screen("knowledge", "Knowledge", Icons.Default.MenuBook)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        Screen.Chat, Screen.Brain, Screen.Files, Screen.Knowledge, Screen.Settings
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

