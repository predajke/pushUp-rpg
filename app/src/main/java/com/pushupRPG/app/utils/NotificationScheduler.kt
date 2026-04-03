package com.pushupRPG.app.utils

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val WORK_TAG_DAILY = "daily_notification"
    private const val WORK_TAG_EVENING = "evening_notification"

    fun scheduleDailyNotifications(context: Context) {
        scheduleNotificationAt(context, 10, 0, WORK_TAG_DAILY)   // 10:00
        scheduleNotificationAt(context, 20, 0, WORK_TAG_EVENING)  // 20:00
    }

    private fun scheduleNotificationAt(
        context: Context,
        hour: Int,
        minute: Int,
        tag: String
    ) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // Если время уже прошло сегодня — ставим на завтра
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis

        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(tag)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            tag,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG_DAILY)
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG_EVENING)
    }
}