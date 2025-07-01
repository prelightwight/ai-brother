package com.prelightwight.aibrother.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Chat : Screen("chat", "Chat", { Icon(Icons.Default.Chat, contentDescription = null) })
    object Brain : Screen("brain", "Brain", { Icon(Icons.Default.Memory, contentDescription = null) })
    object Files : Screen("files", "Files", { Icon(Icons.Default.Folder, contentDescription = null) })
    object Knowledge : Screen("knowledge", "Knowledge", { Icon(Icons.Default.MenuBook, contentDescription = null) })
    object Search : Screen("search", "Search", { Icon(Icons.Default.Search, contentDescription = null) })
    object Images : Screen("images", "Images", { Icon(Icons.Default.Image, contentDescription = null) })
    object Settings : Screen("settings", "Settings", { Icon(Icons.Default.Settings, contentDescription = null) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavBar(navController: NavHostController) {
    val primaryItems = listOf(
        Screen.Chat, Screen.Brain, Screen.Files, Screen.Knowledge
    )
    
    val secondaryItems = listOf(
        Screen.Search, Screen.Images, Screen.Settings
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Primary navigation items (always visible)
        primaryItems.forEach { screen ->
            NavigationBarItem(
                icon = screen.icon,
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

        // Overflow menu for secondary items
        var showOverflowMenu by remember { mutableStateOf(false) }
        
        NavigationBarItem(
            icon = { 
                Icon(
                    Icons.Default.MoreHoriz, 
                    contentDescription = "More",
                    tint = if (secondaryItems.any { it.route == currentRoute }) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            label = { Text("More") },
            selected = secondaryItems.any { it.route == currentRoute },
            onClick = { showOverflowMenu = true }
        )

        // Dropdown menu for overflow items
        DropdownMenu(
            expanded = showOverflowMenu,
            onDismissRequest = { showOverflowMenu = false }
        ) {
            secondaryItems.forEach { screen ->
                DropdownMenuItem(
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            screen.icon()
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(screen.label)
                        }
                    },
                    onClick = {
                        showOverflowMenu = false
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
}

