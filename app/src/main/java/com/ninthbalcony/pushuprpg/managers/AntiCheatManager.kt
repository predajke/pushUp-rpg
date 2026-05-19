package com.ninthbalcony.pushuprpg.managers

import com.ninthbalcony.pushuprpg.data.db.GameStateEntity

enum class AdType {
    NONE,           // 1-2й раз — без рекламы
    SKIPPABLE,      // 3й раз — 20-30s с skip после 10s
    NO_SKIP         // 5й+ раз — 20-30s без skip
}

class AntiCheatManager {

    private var lastGeneralSaveTime: Long = 0L

    fun checkGeneralCooldown(count: Int): Long {
        val elapsed = System.currentTimeMillis() - lastGeneralSaveTime
        if (elapsed >= 10_000L) return 0L
        val cooldown = generalCooldownMs(count)
        return (cooldown - elapsed).coerceAtLeast(0L)
    }

    fun recordGeneralSave() {
        lastGeneralSaveTime = System.currentTimeMillis()
    }

    private fun generalCooldownMs(count: Int): Long = when (count) {
        in 1..10  ->  8_000L
        in 11..20 -> 12_000L
        in 21..40 -> 15_000L
        else      -> 20_000L
    }

    fun generalAdType(count: Int): AdType =
        if (count >= 41) AdType.SKIPPABLE else AdType.NONE

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
