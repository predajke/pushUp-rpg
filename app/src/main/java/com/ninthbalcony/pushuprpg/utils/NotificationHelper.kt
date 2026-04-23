package com.ninthbalcony.pushuprpg.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ninthbalcony.pushuprpg.MainActivity
import com.ninthbalcony.pushuprpg.R

object NotificationHelper {

    private const val CHANNEL_ID = "pushup_rpg_channel"
    private const val CHANNEL_NAME = "Push UP RPG"
    private const val NOTIFICATION_ID_REMINDER = 1001
    private const val NOTIFICATION_ID_HERO_DEAD = 1002
    private const val NOTIFICATION_ID_EVENT = 1003
    private const val NOTIFICATION_ID_SPIN_READY = 1004

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Push UP RPG notifications"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun getMainActivityIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showDailyReminderNotification(context: Context) {
        val messages = listOf(
            "💪 Ты сегодня не отжимался! Герой ждёт тебя." to
                    "💪 You haven't done push-ups today! Your hero is waiting.",
            "🔥 Стрик под угрозой! Зайди и отожмись." to
                    "🔥 Your streak is at risk! Come and do push-ups.",
            "⚔️ Твой герой скучает без тебя. Пора тренироваться!" to
                    "⚔️ Your hero misses you. Time to train!",
            "🏆 Не теряй прогресс! Сделай хотя бы 10 отжиманий." to
                    "🏆 Don't lose your progress! Do at least 10 push-ups."
        )
        val (titleRu, titleEn) = messages.random()

        // Определяем язык из SharedPreferences
        val prefs = context.getSharedPreferences("pushup_prefs", Context.MODE_PRIVATE)
        val language = prefs.getString("language", "en") ?: "en"
        val text = if (language == "ru") titleRu else titleEn

        showNotification(
            context = context,
            id = NOTIFICATION_ID_REMINDER,
            title = if (language == "ru") "Push UP RPG" else "Push UP RPG",
            text = text
        )
    }

    fun showHeroDeadNotification(context: Context, heroName: String, language: String) {
        val title = if (language == "ru") "⚰️ $heroName пал в бою!" else "⚰️ $heroName has fallen!"
        val text = if (language == "ru")
            "Герой ждёт воскрешения. Зайди и отожмись чтобы возродить его!"
        else
            "Your hero awaits revival. Come and do push-ups to revive!"

        showNotification(
            context = context,
            id = NOTIFICATION_ID_HERO_DEAD,
            title = title,
            text = text
        )
    }

    fun showEventNotification(context: Context, eventName: String, language: String) {
        val title = if (language == "ru") "🎉 Новое событие!" else "🎉 New Event!"
        val text = if (language == "ru")
            "$eventName — зайди чтобы воспользоваться бонусом!"
        else
            "$eventName — log in to use the bonus!"

        showNotification(
            context = context,
            id = NOTIFICATION_ID_EVENT,
            title = title,
            text = text
        )
    }

    fun showSpinReadyNotification(context: Context) {
        val prefs = context.getSharedPreferences("pushup_prefs", Context.MODE_PRIVATE)
        val language = prefs.getString("language", "en") ?: "en"
        val title = if (language == "ru") "🎰 Спины накопились!" else "🎰 Spins are ready!"
        val text = if (language == "ru")
            "У тебя накопилось 5 прокрутов колеса удачи. Зайди и используй их!"
        else
            "You have 5 spin tokens waiting. Come and use them!"
        showNotification(context, NOTIFICATION_ID_SPIN_READY, title, text)
    }

    private fun showNotification(
        context: Context,
        id: Int,
        title: String,
        text: String
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getMainActivityIntent(context))
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(id, notification)
    }
}