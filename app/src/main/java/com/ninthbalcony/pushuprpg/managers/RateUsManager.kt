package com.ninthbalcony.pushuprpg.managers

import android.util.Log

/**
 * RateUsManager controls when to show the "Rate Us" dialog.
 *
 * Logic:
 * - Show dialog 3 days after installation
 * - Show once every 2 days (cooldown)
 * - "Never show again" option disables forever
 */
class RateUsManager {
    companion object {
        private const val TAG = "RateUsManager"
        private const val DAYS_BEFORE_FIRST_SHOW = 3
        private const val COOLDOWN_DAYS = 2
        private const val MS_PER_DAY = 24 * 60 * 60 * 1000L
        private val FIRST_SHOW_DELAY_MS = DAYS_BEFORE_FIRST_SHOW * MS_PER_DAY
        private val COOLDOWN_MS = COOLDOWN_DAYS * MS_PER_DAY
    }

    private var installDate: Long = 0L
    private var lastShowDate: Long = 0L
    private var doNotShowAgain: Boolean = false

    /**
     * Initialize manager with database values.
     */
    fun initialize(installDate: Long, lastShowDate: Long, doNotShowAgain: Boolean) {
        this.installDate = installDate
        this.lastShowDate = lastShowDate
        this.doNotShowAgain = doNotShowAgain
        Log.d(TAG, "RateUsManager initialized: installDate=$installDate, lastShowDate=$lastShowDate, doNotShowAgain=$doNotShowAgain")
    }

    /**
     * Check if Rate Us dialog should be shown.
     */
    fun shouldShowDialog(): Boolean {
        if (doNotShowAgain) {
            return false
        }

        val currentTime = System.currentTimeMillis()
        val timeSinceInstall = currentTime - installDate

        // First show: 3 days after install
        if (lastShowDate == 0L) {
            return timeSinceInstall >= FIRST_SHOW_DELAY_MS
        }

        // Subsequent shows: every 2 days
        val timeSinceLastShow = currentTime - lastShowDate
        return timeSinceLastShow >= COOLDOWN_MS
    }

    /**
     * Mark dialog as shown (called when dialog appears).
     */
    fun markAsShown() {
        lastShowDate = System.currentTimeMillis()
        Log.d(TAG, "Rate Us dialog shown at: $lastShowDate")
    }

    /**
     * Mark "Never show again" (called when user clicks "Don't show").
     */
    fun markDoNotShowAgain() {
        doNotShowAgain = true
        Log.d(TAG, "Rate Us marked as: never show again")
    }

    /**
     * Get days remaining before first show (or 0 if should show now).
     */
    fun getDaysBeforeFirstShow(): Int {
        val timeSinceInstall = System.currentTimeMillis() - installDate
        val remainingMs = FIRST_SHOW_DELAY_MS - timeSinceInstall
        if (remainingMs <= 0) {
            return 0
        }
        return (remainingMs / MS_PER_DAY).toInt() + 1
    }

    /**
     * Get days remaining for cooldown (or 0 if can show now).
     */
    fun getDaysBeforeNextShow(): Int {
        if (lastShowDate == 0L) {
            return getDaysBeforeFirstShow()
        }

        val timeSinceLastShow = System.currentTimeMillis() - lastShowDate
        val remainingMs = COOLDOWN_MS - timeSinceLastShow
        if (remainingMs <= 0) {
            return 0
        }
        return (remainingMs / MS_PER_DAY).toInt() + 1
    }
}
