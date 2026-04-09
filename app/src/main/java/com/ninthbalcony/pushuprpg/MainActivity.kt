package com.ninthbalcony.pushuprpg

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
import com.ninthbalcony.pushuprpg.data.repository.GameRepository
import com.ninthbalcony.pushuprpg.managers.AdManager
import com.ninthbalcony.pushuprpg.managers.AntiCheatManager
import com.ninthbalcony.pushuprpg.managers.CloudSyncManager
import com.ninthbalcony.pushuprpg.managers.OnboardingManager
import com.ninthbalcony.pushuprpg.managers.PlayGamesManager
import com.ninthbalcony.pushuprpg.managers.RateUsManager
import com.ninthbalcony.pushuprpg.ui.AppNavigation
import com.ninthbalcony.pushuprpg.ui.GameViewModel
import com.ninthbalcony.pushuprpg.ui.theme.DarkBackground
import com.ninthbalcony.pushuprpg.ui.theme.PushUpRPGTheme
import com.ninthbalcony.pushuprpg.utils.NotificationHelper
import com.ninthbalcony.pushuprpg.utils.NotificationScheduler
import kotlinx.coroutines.GlobalScope

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var cloudSyncManager: CloudSyncManager
    private lateinit var adManager: AdManager
    private lateinit var playGamesManager: PlayGamesManager
    private lateinit var antiCheatManager: AntiCheatManager
    private lateinit var rateUsManager: RateUsManager
    private lateinit var onboardingManager: OnboardingManager

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
        antiCheatManager = AntiCheatManager()
        rateUsManager = RateUsManager()
        onboardingManager = OnboardingManager()

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
        onboardingManager.reset()
        Log.d(TAG, "MainActivity destroyed")
    }
}
