package com.pushupRPG.app.managers

import android.util.Log
import com.pushupRPG.app.data.db.GameStateEntity

class AntiCheatManager {
    companion object {
        private const val TAG = "AntiCheatManager"
        private const val RAPID_SAVE_THRESHOLD = 80  // pushups
        private const val RAPID_SAVE_WINDOW_MS = 10_000L  // 10 seconds
    }

    fun isRapidSaveDetected(
        currentGameState: GameStateEntity,
        previousGameState: GameStateEntity
    ): Boolean {
        val pushupDelta = currentGameState.totalPushUpsAllTime - previousGameState.totalPushUpsAllTime
        val timeDelta = System.currentTimeMillis() - currentGameState.lastSaveTime

        val isRapidSave = pushupDelta >= RAPID_SAVE_THRESHOLD && timeDelta <= RAPID_SAVE_WINDOW_MS

        if (isRapidSave) {
            Log.w(
                TAG,
                "Rapid save detected: $pushupDelta pushups in ${timeDelta}ms (threshold: $RAPID_SAVE_THRESHOLD/$RAPID_SAVE_WINDOW_MS)"
            )
        }

        return isRapidSave
    }

    fun getRemainingCooldownMs(lastSaveTime: Long): Long {
        val timeSinceLastSave = System.currentTimeMillis() - lastSaveTime
        val remainingCooldown = RAPID_SAVE_WINDOW_MS - timeSinceLastSave
        return if (remainingCooldown > 0) remainingCooldown else 0
    }

    fun isCooldownActive(lastSaveTime: Long): Boolean {
        return getRemainingCooldownMs(lastSaveTime) > 0
    }
}
