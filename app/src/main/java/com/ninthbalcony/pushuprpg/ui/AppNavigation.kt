package com.ninthbalcony.pushuprpg.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ninthbalcony.pushuprpg.ui.screens.InventoryScreen
import com.ninthbalcony.pushuprpg.ui.screens.LoginScreen
import com.ninthbalcony.pushuprpg.ui.screens.LogsScreen
import com.ninthbalcony.pushuprpg.ui.screens.MainMenuScreen
import com.ninthbalcony.pushuprpg.ui.screens.QuestsScreen
import com.ninthbalcony.pushuprpg.ui.screens.SettingsScreen
import com.ninthbalcony.pushuprpg.ui.screens.ShopScreen
import com.ninthbalcony.pushuprpg.ui.screens.StatisticsScreen
import com.ninthbalcony.pushuprpg.ui.screens.SplashScreen
import com.ninthbalcony.pushuprpg.ui.screens.ProgressScreen
import com.ninthbalcony.pushuprpg.ui.screens.AchievementsScreen
import com.ninthbalcony.pushuprpg.ui.screens.BestiaryScreen
import com.ninthbalcony.pushuprpg.ui.screens.ItemLogScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val MAIN_MENU = "main_menu"
    const val INVENTORY = "inventory"
    const val LOGS = "logs"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val SHOP = "shop"
    const val QUESTS = "quests"
    const val PROGRESS = "progress"
    const val ACHIEVEMENTS = "achievements"
    const val BESTIARY = "bestiary"
    const val ITEM_LOG = "item_log"
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
                onNavigateToInventory = { navController.navigate(Routes.INVENTORY) },
                onNavigateToLogs = { navController.navigate(Routes.LOGS) },
                onNavigateToStatistics = { navController.navigate(Routes.STATISTICS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToShop = { navController.navigate(Routes.SHOP) },
                onNavigateToQuests = { navController.navigate(Routes.QUESTS) },
                onNavigateToProgress = { navController.navigate(Routes.PROGRESS) }
            )
        }

        // Inventory
        composable(Routes.INVENTORY) {
            InventoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToShop = { navController.navigate(Routes.SHOP) },
                onNavigateToAchievements = { navController.navigate(Routes.ACHIEVEMENTS) }
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

        // Quests
        composable(Routes.QUESTS) {
            QuestsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Progress Hub
        composable(Routes.PROGRESS) {
            ProgressScreen(
                viewModel = viewModel,
                onNavigateToAchievements = { navController.navigate(Routes.ACHIEVEMENTS) },
                onNavigateToBestiary = { navController.navigate(Routes.BESTIARY) },
                onNavigateToItemLog = { navController.navigate(Routes.ITEM_LOG) },
                onBack = { navController.popBackStack() }
            )
        }

        // Achievements
        composable(Routes.ACHIEVEMENTS) {
            AchievementsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Bestiary
        composable(Routes.BESTIARY) {
            BestiaryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Item Log
        composable(Routes.ITEM_LOG) {
            ItemLogScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}