package com.pushupRPG.app

import android.os.Bundle
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
import com.pushupRPG.app.data.repository.GameRepository
import com.pushupRPG.app.ui.AppNavigation
import com.pushupRPG.app.ui.GameViewModel
import com.pushupRPG.app.ui.theme.DarkBackground
import com.pushupRPG.app.ui.theme.PushUpRPGTheme
import com.pushupRPG.app.utils.NotificationHelper
import com.pushupRPG.app.utils.NotificationScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Создаём канал уведомлений
        NotificationHelper.createNotificationChannel(this)

        // Запрашиваем разрешение на уведомления (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        // Планируем ежедневные уведомления
        NotificationScheduler.scheduleDailyNotifications(this)

        setContent {
            PushUpRPGTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    // Получаем контекст для базы данных
                    val context = LocalContext.current

                    // Создаем ViewModel через Factory, передавая туда наш GameRepository
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
}