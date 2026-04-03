package com.pushupRPG.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pushupRPG.app.ui.screens.InventoryScreen
import com.pushupRPG.app.ui.screens.LoginScreen
import com.pushupRPG.app.ui.screens.LogsScreen
import com.pushupRPG.app.ui.screens.MainMenuScreen
import com.pushupRPG.app.ui.screens.SettingsScreen
import com.pushupRPG.app.ui.screens.ShopScreen
import com.pushupRPG.app.ui.screens.StatisticsScreen
import com.pushupRPG.app.ui.screens.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val MAIN_MENU = "main_menu"
    const val INVENTORY = "inventory"
    const val LOGS = "logs"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val SHOP = "shop"
}

@Composable
fun AppNavigation(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val gameState by viewModel.gameState.collectAsState(initial = null)

    // Определяем стартовый экран
    val startDestination = if (gameState != null) {
        Routes.MAIN_MENU
    } else {
        Routes.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        // Splash Screen
        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN_MENU) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Main Menu
        composable(Routes.MAIN_MENU) {
            MainMenuScreen(
                viewModel = viewModel,
                onNavigateToInventory = {
                    navController.navigate(Routes.INVENTORY)
                },
                onNavigateToLogs = {
                    navController.navigate(Routes.LOGS)
                },
                onNavigateToStatistics = {
                    navController.navigate(Routes.STATISTICS)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateToShop = {
                    navController.navigate(Routes.SHOP)
                }
            )
        }

        // Inventory
        composable(Routes.INVENTORY) {
            InventoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToShop = {
                    navController.navigate(Routes.SHOP)
                }
            )
        }

        // Logs
        composable(Routes.LOGS) {
            LogsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Statistics
        composable(Routes.STATISTICS) {
            StatisticsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Settings
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Shop
        composable(Routes.SHOP) {
            ShopScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}