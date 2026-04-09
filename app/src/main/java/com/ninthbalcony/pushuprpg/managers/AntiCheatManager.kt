package com.ninthbalcony.pushuprpg.managers

import android.util.Log
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity

enum class AdType {
    NONE,           // 1-2й раз — без рекламы
    SKIPPABLE,      // 3й раз — 20-30s с skip после 10s
    NO_SKIP         // 5й+ раз — 20-30s без skip
}

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

    // Max pushups (99) tracking logic
    fun getRequiredAd(attemptNumber: Int): AdType {
        return when (attemptNumber) {
            1, 2 -> AdType.NONE           // 1-2й раз: без рекламы
            3, 4 -> AdType.SKIPPABLE      // 3-4й раз: 20-30s с skip
            else -> AdType.NO_SKIP        // 5й+ раз: без skip
        }
    }

    fun calculateCooldownMs(attemptNumber: Int): Long {
        return when (attemptNumber) {
            1, 2 -> 12_000L               // 12 сек
            3 -> 15_000L                  // 15 сек
            4 -> 18_000L                  // 18 сек
            5 -> 20_000L                  // 20 сек
            else -> 25_000L               // 25 сек (6+)
        }
    }

    fun isCooldownActiveForMaxAttempt(lastMaxAttemptTime: Long, attemptNumber: Int): Boolean {
        return getRemainingCooldownMsForMaxAttempt(lastMaxAttemptTime, attemptNumber) > 0
    }

    fun getRemainingCooldownMsForMaxAttempt(lastMaxAttemptTime: Long, attemptNumber: Int): Long {
        val timeSinceLastAttempt = System.currentTimeMillis() - lastMaxAttemptTime
        val cooldownDuration = calculateCooldownMs(attemptNumber)
        val remainingCooldown = cooldownDuration - timeSinceLastAttempt
        return if (remainingCooldown > 0) remainingCooldown else 0
    }
}
