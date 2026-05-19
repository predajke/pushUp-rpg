package com.ninthbalcony.pushuprpg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.ninthbalcony.pushuprpg.ui.AppNavigation
import com.ninthbalcony.pushuprpg.ui.GameViewModel
import com.ninthbalcony.pushuprpg.ui.theme.DarkBackground
import com.ninthbalcony.pushuprpg.ui.theme.PushUpRPGTheme
import com.ninthbalcony.pushuprpg.utils.NotificationHelper
import com.ninthbalcony.pushuprpg.utils.NotificationScheduler
import com.ninthbalcony.pushuprpg.utils.SoundManager
import androidx.lifecycle.lifecycleScope

class MainActivity : ComponentActivity() {

    private lateinit var cloudSyncManager: CloudSyncManager
    private lateinit var adManager: AdManager
    private lateinit var playGamesManager: PlayGamesManager
    private lateinit var antiCheatManager: AntiCheatManager
    private lateinit var onboardingManager: OnboardingManager

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) NotificationScheduler.scheduleDailyNotifications(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase
        try { Firebase.initialize(this) } catch (_: Exception) {}

        // Initialize managers
        cloudSyncManager = CloudSyncManager(this, lifecycleScope)
        adManager = AdManager(this)
        adManager.preloadRewardedAd()
        playGamesManager = PlayGamesManager(this, lifecycleScope)
        antiCheatManager = AntiCheatManager()
        onboardingManager = OnboardingManager()
        SoundManager.init(this)

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Request notification permission (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // Schedule daily notifications
        NotificationScheduler.scheduleDailyNotifications(this)

        // Silent Play Games sign-in
        try {
            playGamesManager.signIn(this) {}
        } catch (_: Exception) {}

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

                    viewModel.setAdManager(adManager)
                    viewModel.setPlayGamesManager(playGamesManager)
                    viewModel.setCloudSyncManager(cloudSyncManager)
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        SoundManager.pauseWithFade()
    }

    override fun onResume() {
        super.onResume()
        SoundManager.resumeWithFade()
    }

    override fun onDestroy() {
        super.onDestroy()
        cloudSyncManager.destroy()
        adManager.destroy()
        playGamesManager.signOut(this)
        onboardingManager.reset()
        SoundManager.release()
    }
}
