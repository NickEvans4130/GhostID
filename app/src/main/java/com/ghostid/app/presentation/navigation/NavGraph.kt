package com.ghostid.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ghostid.app.presentation.ui.screens.AliasDetailScreen
import com.ghostid.app.presentation.ui.screens.MainScreen
import com.ghostid.app.presentation.ui.screens.MessageDetailScreen
import com.ghostid.app.presentation.ui.screens.PasswordVaultScreen
import com.ghostid.app.presentation.ui.screens.SettingsScreen

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val bottomItems = listOf(
        BottomNavItem(Screen.Main, "Aliases", Icons.Default.Person),
        BottomNavItem(Screen.PasswordVault, "Vault", Icons.Default.Lock),
        BottomNavItem(Screen.Settings, "Settings", Icons.Default.Settings),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination
    val showBottomBar = bottomItems.any { item ->
        currentDest?.hierarchy?.any { it.route == item.screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentDest?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Main.route) {
                MainScreen(
                    onAliasClick = { aliasId ->
                        navController.navigate(Screen.AliasDetail.createRoute(aliasId))
                    }
                )
            }
            composable(
                route = Screen.AliasDetail.route,
                arguments = listOf(navArgument("aliasId") { type = NavType.StringType }),
            ) {
                AliasDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMessage = { aliasId, messageId ->
                        navController.navigate(Screen.MessageDetail.createRoute(aliasId, messageId))
                    },
                )
            }
            composable(
                route = Screen.MessageDetail.route,
                arguments = listOf(
                    navArgument("aliasId") { type = NavType.StringType },
                    navArgument("messageId") { type = NavType.StringType },
                ),
            ) {
                MessageDetailScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.PasswordVault.route) {
                PasswordVaultScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
