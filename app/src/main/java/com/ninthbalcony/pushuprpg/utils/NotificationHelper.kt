package com.ninthbalcony.pushuprpg.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ninthbalcony.pushuprpg.MainActivity
import com.ninthbalcony.pushuprpg.R
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity

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

    /**
     * Daily reminder с **персонализированным** текстом по приоритету:
     *   1. Streak под угрозой (есть streak ≥ 1, сегодня нет отжиманий) → защити его.
     *   2. Монстр почти убит (HP < 20%) → доходчиво «доделай».
     *   3. Есть unspent stat points → потрать их.
     *   4. Завтра milestone-награда (streak + 1 день = next milestone) → не сорви.
     *   5. Дефолт — generic ротация из 4 шаблонов.
     *
     * Если state == null или ошибка — выбираем дефолт.
     */
    fun showDailyReminderNotification(context: Context, state: GameStateEntity? = null, language: String? = null) {
        val lang = language ?: run {
            val prefs = context.getSharedPreferences("pushup_prefs", Context.MODE_PRIVATE)
            prefs.getString("language", "en") ?: "en"
        }
        val ru = lang == "ru"

        val text: String = state?.let { pickPersonalizedReminder(it, ru) } ?: defaultReminder(ru)

        showNotification(
            context = context,
            id = NOTIFICATION_ID_REMINDER,
            title = "Push UP RPG",
            text = text
        )
    }

    private fun pickPersonalizedReminder(state: GameStateEntity, ru: Boolean): String {
        // 1. Streak в опасности — самый высокий приоритет, самая болезненная потеря.
        if (state.currentStreak >= 1 && state.pushUpsToday == 0) {
            return if (ru) "🔥 Не теряй streak ${state.currentStreak} дней! 30 отжиманий — и день засчитан."
                   else "🔥 Don't lose your ${state.currentStreak}-day streak! 30 push-ups and the day counts."
        }
        // 2. Монстр почти убит.
        if (state.monsterMaxHp > 0 && state.monsterCurrentHp.toFloat() / state.monsterMaxHp < 0.2f) {
            return if (ru) "👹 ${state.monsterName} осталось ${state.monsterCurrentHp} HP. Финиш!"
                   else "👹 ${state.monsterName} has ${state.monsterCurrentHp} HP left. Finish it!"
        }
        // 3. Непотраченные stat points.
        if (state.unspentStatPoints > 0) {
            return if (ru) "⭐ ${state.unspentStatPoints} stat points ждут — открой Inventory."
                   else "⭐ ${state.unspentStatPoints} stat points waiting — open Inventory."
        }
        // 4. Завтра следующий milestone.
        val next = StreakRewards.nextMilestone(state.currentStreak)
        if (next != null && next.day == state.currentStreak + 1) {
            return if (ru) "🎁 День ${next.day} завтра — награда: +${next.teeth} 🦷"
                   else "🎁 Day ${next.day} tomorrow — reward: +${next.teeth} 🦷"
        }
        // 5. Дефолт.
        return defaultReminder(ru)
    }

    private fun defaultReminder(ru: Boolean): String {
        val messages = listOf(
            "💪 Ты сегодня не отжимался! Герой ждёт тебя." to
                    "💪 You haven't done push-ups today! Your hero is waiting.",
            "⚔️ Твой герой скучает без тебя. Пора тренироваться!" to
                    "⚔️ Your hero misses you. Time to train!",
            "🏆 Не теряй прогресс! Сделай хотя бы 10 отжиманий." to
                    "🏆 Don't lose your progress! Do at least 10 push-ups."
        )
        val (titleRu, titleEn) = messages.random()
        return if (ru) titleRu else titleEn
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