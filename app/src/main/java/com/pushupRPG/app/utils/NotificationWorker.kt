package com.pushupRPG.app.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pushupRPG.app.data.db.AppDatabase

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.pushUpDao()
        val state = dao.getGameState() ?: return Result.success()

        val today = DateUtils.getTodayString()
        val prefs = applicationContext.getSharedPreferences(
            "pushup_prefs", Context.MODE_PRIVATE
        )
        val language = prefs.getString("language", "en") ?: "en"

        // Уведомление если герой мёртв
        if (state.isPlayerDead) {
            NotificationHelper.showHeroDeadNotification(
                applicationContext,
                state.playerName,
                language
            )
            return Result.success()
        }

        // Уведомление если не было отжиманий сегодня
        if (state.lastResetDate != today || state.pushUpsToday == 0) {
            NotificationHelper.showDailyReminderNotification(applicationContext)
        }

        return Result.success()
    }
}