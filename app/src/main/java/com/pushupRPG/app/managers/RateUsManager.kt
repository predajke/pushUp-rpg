package com.pushupRPG.app.managers

import android.util.Log

class RateUsManager {
    companion object {
        private const val TAG = "RateUsManager"
        private const val INITIAL_DELAY_DAYS = 3L
        private const val COOLDOWN_DAYS = 2L
        private const val MS_PER_DAY = 86_400_000L
    }

    fun shouldShowRateUsDialog(
        installDate: Long,
        rateUsLastShowDate: Long,
        rateUsDoNotShowAgain: Boolean
    ): Boolean {
        if (rateUsDoNotShowAgain) {
            Log.d(TAG, "User selected 'Never' - not showing Rate Us dialog")
            return false
        }

        val currentTime = System.currentTimeMillis()
        val timeSinceInstall = currentTime - installDate

        // Check if initial delay has passed (3 days)
        if (timeSinceInstall < INITIAL_DELAY_DAYS * MS_PER_DAY) {
            Log.d(TAG, "Initial delay not yet passed - not showing Rate Us dialog")
            return false
        }

        // If never shown before, show after 3 days
        if (rateUsLastShowDate == 0L) {
            Log.d(TAG, "Showing Rate Us dialog for the first time after 3-day delay")
            return true
        }

        // Check cooldown (2 days)
        val timeSinceLastShow = currentTime - rateUsLastShowDate
        if (timeSinceLastShow < COOLDOWN_DAYS * MS_PER_DAY) {
            Log.d(TAG, "Still in cooldown period - not showing Rate Us dialog")
            return false
        }

        Log.d(TAG, "Showing Rate Us dialog after cooldown period")
        return true
    }

    fun getRemainingCooldownMs(rateUsLastShowDate: Long): Long {
        val timeSinceLastShow = System.currentTimeMillis() - rateUsLastShowDate
        val remainingCooldown = (COOLDOWN_DAYS * MS_PER_DAY) - timeSinceLastShow
        return if (remainingCooldown > 0) remainingCooldown else 0
    }

    fun getRemainingInitialDelayMs(installDate: Long): Long {
        val timeSinceInstall = System.currentTimeMillis() - installDate
        val remainingDelay = (INITIAL_DELAY_DAYS * MS_PER_DAY) - timeSinceInstall
        return if (remainingDelay > 0) remainingDelay else 0
    }
}
