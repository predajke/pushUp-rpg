package com.ninthbalcony.pushuprpg.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
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
import com.ninthbalcony.pushuprpg.utils.AppStrings
import com.ninthbalcony.pushuprpg.ui.screens.AchievementsScreen
import com.ninthbalcony.pushuprpg.ui.screens.BestiaryScreen
import com.ninthbalcony.pushuprpg.ui.screens.ItemLogScreen
import com.ninthbalcony.pushuprpg.ui.screens.LeaderboardScreen

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
    const val BOSSES = "bosses"
    const val ITEM_LOG = "item_log"
    const val LEADERBOARD = "leaderboard"
}

@Composable
fun AppNavigation(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val context = LocalContext.current
    val language = gameState?.language ?: "en"

    // Cloud restore Toast — слушаем событие один раз за жизнь Activity.
    LaunchedEffect(viewModel) {
        viewModel.restoreEvent?.collect { result ->
            when (result) {
                is com.ninthbalcony.pushuprpg.managers.RestoreResult.Restored -> {
                    val msg = "${AppStrings.t(language, "cloud_restored")} (Lvl ${result.level}, ${result.totalPushUps} ${AppStrings.t(language, "push_ups_short")})"
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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
                onNavigateToInventory = { navController.navigate(Routes.INVENTORY) { launchSingleTop = true } },
                onNavigateToLogs = { navController.navigate(Routes.LOGS) { launchSingleTop = true } },
                onNavigateToStatistics = { navController.navigate(Routes.STATISTICS) { launchSingleTop = true } },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                onNavigateToShop = { navController.navigate(Routes.SHOP) { launchSingleTop = true } },
                onNavigateToQuests = { navController.navigate(Routes.QUESTS) { launchSingleTop = true } },
                onNavigateToProgress = { navController.navigate(Routes.PROGRESS) { launchSingleTop = true } },
                onNavigateToLeaderboard = { navController.navigate(Routes.LEADERBOARD) { launchSingleTop = true } }
            )
        }

        // Inventory
        composable(Routes.INVENTORY) {
            InventoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack(Routes.MAIN_MENU, inclusive = false) },
                onNavigateToShop = { navController.navigate(Routes.SHOP) { launchSingleTop = true } },
                onNavigateToAchievements = { navController.navigate(Routes.ACHIEVEMENTS) { launchSingleTop = true } }
            )
        }

        // Logs
        composable(Routes.LOGS) {
            LogsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack(Routes.MAIN_MENU, inclusive = false) }
            )
        }

        // Statistics
        composable(Routes.STATISTICS) {
            StatisticsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack(Routes.MAIN_MENU, inclusive = false) }
            )
        }

        // Settings
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack(Routes.MAIN_MENU, inclusive = false) }
            )
        }

        // Shop
        composable(Routes.SHOP) {
            ShopScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack(Routes.MAIN_MENU, inclusive = false) }
            )
        }

        // Quests
        composable(Routes.QUESTS) {
            QuestsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack(Routes.MAIN_MENU, inclusive = false) }
            )
        }

        // Progress Hub
        composable(Routes.PROGRESS) {
            ProgressScreen(
                viewModel = viewModel,
                onNavigateToAchievements = { navController.navigate(Routes.ACHIEVEMENTS) { launchSingleTop = true } },
                onNavigateToBestiary = { navController.navigate(Routes.BESTIARY) { launchSingleTop = true } },
                onNavigateToBosses = { navController.navigate(Routes.BOSSES) { launchSingleTop = true } },
                onNavigateToItemLog = { navController.navigate(Routes.ITEM_LOG) { launchSingleTop = true } },
                onBack = { navController.popBackStack(Routes.MAIN_MENU, inclusive = false) }
            )
        }

        // Achievements
        composable(Routes.ACHIEVEMENTS) {
            AchievementsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Bestiary (монстры)
        composable(Routes.BESTIARY) {
            BestiaryScreen(
                viewModel = viewModel,
                bossesOnly = false,
                onBack = { navController.popBackStack() }
            )
        }

        // Bosses
        composable(Routes.BOSSES) {
            BestiaryScreen(
                viewModel = viewModel,
                bossesOnly = true,
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

        // Leaderboard
        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack(Routes.MAIN_MENU, inclusive = false) }
            )
        }
    }
}