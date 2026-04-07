package com.pushupRPG.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.pushupRPG.app.data.repository.GameRepository
import com.pushupRPG.app.managers.AdManager
import com.pushupRPG.app.managers.CloudSyncManager
import com.pushupRPG.app.managers.PlayGamesManager
import com.pushupRPG.app.ui.AppNavigation
import com.pushupRPG.app.ui.GameViewModel
import com.pushupRPG.app.ui.theme.DarkBackground
import com.pushupRPG.app.ui.theme.PushUpRPGTheme
import com.pushupRPG.app.utils.NotificationHelper
import com.pushupRPG.app.utils.NotificationScheduler
import kotlinx.coroutines.GlobalScope

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var cloudSyncManager: CloudSyncManager
    private lateinit var adManager: AdManager
    private lateinit var playGamesManager: PlayGamesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase
        try {
            Firebase.initialize(this)
            Log.d(TAG, "Firebase initialized")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase already initialized: ${e.message}")
        }

        // Initialize managers
        cloudSyncManager = CloudSyncManager(this, GlobalScope)
        adManager = AdManager(this)
        playGamesManager = PlayGamesManager(this)

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Request notification permission (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        // Schedule daily notifications
        NotificationScheduler.scheduleDailyNotifications(this)

        // Initialize Cloud Sync (offline-first, async)
        try {
            cloudSyncManager.initialize { success ->
                Log.d(TAG, "Cloud sync initialized: $success")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cloud sync initialization error: ${e.message}")
        }

        // Silent Play Games sign-in
        try {
            playGamesManager.signIn(this) { success ->
                if (success) {
                    Log.d(TAG, "Play Games signed in")
                } else {
                    Log.d(TAG, "Play Games sign-in skipped or failed (optional)")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Play Games initialization error: ${e.message}")
        }

        setContent {
            PushUpRPGTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    // Get context for database
                    val context = LocalContext.current

                    // Create ViewModel via Factory
                    val viewModel: GameViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                val repository = GameRepository(context)
                                return GameViewModel(repository) as T
                            }
                        }
                    )

                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cloudSyncManager.destroy()
        adManager.destroy()
        playGamesManager.signOut(this)
    }
}
