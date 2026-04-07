package com.ninthbalcony.pushuprpg.managers

import android.util.Log

/**
 * AntiCheatManager detects suspicious behavior (too many push-ups too quickly).
 *
 * Logic:
 * - If user saves 80+ push-ups and tries again within 10 seconds → show warning
 * - Block save until cooldown expires
 * - Non-intrusive (no ads as punishment)
 */
class AntiCheatManager {
    companion object {
        private const val TAG = "AntiCheatManager"
        private const val PUSH_UP_THRESHOLD = 80
        private const val COOLDOWN_SECONDS = 10L
        private const val COOLDOWN_MS = COOLDOWN_SECONDS * 1000
    }

    private var lastSaveTime: Long = 0L
    private var lastSaveAmount: Int = 0

    /**
     * Check if save is allowed based on antiCheat rules.
     * Returns: (isAllowed, secondsToWait)
     */
    fun checkSaveAllowed(pushUpsCount: Int): Pair<Boolean, Long> {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastSave = currentTime - lastSaveTime

        // First save is always allowed
        if (lastSaveTime == 0L) {
            lastSaveTime = currentTime
            lastSaveAmount = pushUpsCount
            return Pair(true, 0L)
        }

        // Check if suspicious pattern: 80+ pushups saved AND another 80+ within 10 seconds
        val isSuspicious = lastSaveAmount >= PUSH_UP_THRESHOLD &&
                pushUpsCount >= PUSH_UP_THRESHOLD &&
                timeSinceLastSave < COOLDOWN_MS

        if (isSuspicious) {
            val secondsToWait = ((COOLDOWN_MS - timeSinceLastSave) / 1000) + 1
            Log.w(TAG, "Suspicious activity detected: $pushUpsCount push-ups after $timeSinceLastSave ms. Wait ${secondsToWait}s")
            return Pair(false, secondsToWait)
        }

        // Update state for next check
        lastSaveTime = currentTime
        lastSaveAmount = pushUpsCount
        return Pair(true, 0L)
    }

    /**
     * Set last save time from database (call on app start).
     */
    fun setLastSaveTimeFromDb(timestamp: Long) {
        lastSaveTime = timestamp
        Log.d(TAG, "Last save time restored from DB: $timestamp")
    }

    /**
     * Set last save time (called after successful save).
     */
    fun updateLastSaveTime(timestamp: Long) {
        lastSaveTime = timestamp
    }

    /**
     * Get current cooldown seconds remaining (or 0 if no cooldown).
     */
    fun getCooldownSecondsRemaining(): Long {
        if (lastSaveTime == 0L || lastSaveAmount < PUSH_UP_THRESHOLD) {
            return 0L
        }

        val timeSinceLastSave = System.currentTimeMillis() - lastSaveTime
        if (timeSinceLastSave >= COOLDOWN_MS) {
            return 0L
        }

        return ((COOLDOWN_MS - timeSinceLastSave) / 1000) + 1
    }
}
