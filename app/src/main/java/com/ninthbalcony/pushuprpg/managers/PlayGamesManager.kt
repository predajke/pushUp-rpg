package com.ninthbalcony.pushuprpg.managers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.games.PlayGames
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

/**
 * PlayGamesManager handles Google Play Games achievements.
 *
 * Achievements:
 * - First Fight: Kill 1 monster
 * - Master Pushups: 1000 total push-ups (incremental)
 *
 * TODO: Add actual achievement IDs from Play Console:
 * - ACHIEVEMENT_FIRST_FIGHT = "CgkI..."
 * - ACHIEVEMENT_MASTER_PUSHUPS = "CgkI..."
 */
class PlayGamesManager(private val context: Context) {
    companion object {
        private const val TAG = "PlayGamesManager"

        // TODO: Replace with actual achievement IDs from Play Games Console
        private const val ACHIEVEMENT_FIRST_FIGHT = "CgkI_placeholder_first_fight"
        private const val ACHIEVEMENT_MASTER_PUSHUPS = "CgkI_placeholder_master_pushups"

        private const val MASTER_PUSHUPS_TARGET = 1000
        private const val FIRST_FIGHT_THRESHOLD = 1
    }

    private var isSignedIn = false

    /**
     * Initialize Play Games sign-in.
     * Call from MainActivity onCreate().
     */
    fun signIn(activity: Activity, onSignInComplete: (Boolean) -> Unit) {
        Log.d(TAG, "Attempting Play Games sign-in...")

        try {
            PlayGames.getGamesClient(activity)
                .signIn()
                .addOnSuccessListener {
                    isSignedIn = true
                    Log.d(TAG, "Play Games sign-in successful")
                    onSignInComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Play Games sign-in failed: ${e.message}")
                    onSignInComplete(false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in exception: ${e.message}")
            onSignInComplete(false)
        }
    }

    /**
     * Unlock achievement (called once per achievement).
     */
    fun unlockAchievement(activity: Activity, achievementId: String) {
        if (!isSignedIn) {
            Log.w(TAG, "Not signed in, cannot unlock achievement")
            return
        }

        try {
            PlayGames.getGamesClient(activity).unlockAchievement(achievementId)
            Log.d(TAG, "Achievement unlocked: $achievementId")
        } catch (e: Exception) {
            Log.w(TAG, "Unlock achievement error: ${e.message}")
        }
    }

    /**
     * Increment achievement progress (for incremental achievements).
     */
    fun incrementAchievementProgress(activity: Activity, achievementId: String, increment: Int = 1) {
        if (!isSignedIn) {
            Log.w(TAG, "Not signed in, cannot increment achievement")
            return
        }

        try {
            PlayGames.getGamesClient(activity).incrementAchievement(achievementId, increment)
            Log.d(TAG, "Achievement progress incremented: $achievementId by $increment")
        } catch (e: Exception) {
            Log.w(TAG, "Increment achievement error: ${e.message}")
        }
    }

    /**
     * Unlock first fight achievement (triggered when monsters killed >= 1).
     */
    fun unlockFirstFight(activity: Activity, monstersKilled: Int) {
        if (monstersKilled >= FIRST_FIGHT_THRESHOLD) {
            unlockAchievement(activity, ACHIEVEMENT_FIRST_FIGHT)
        }
    }

    /**
     * Update master pushups achievement (incremental).
     */
    fun updateMasterPushups(activity: Activity, totalPushups: Int) {
        if (!isSignedIn) {
            return
        }

        try {
            // Only increment if we haven't reached the target
            if (totalPushups <= MASTER_PUSHUPS_TARGET) {
                incrementAchievementProgress(activity, ACHIEVEMENT_MASTER_PUSHUPS, 1)
            } else {
                // Auto-unlock if target reached
                unlockAchievement(activity, ACHIEVEMENT_MASTER_PUSHUPS)
            }

            Log.d(TAG, "Master pushups progress: $totalPushups / $MASTER_PUSHUPS_TARGET")
        } catch (e: Exception) {
            Log.w(TAG, "Update master pushups error: ${e.message}")
        }
    }

    /**
     * Check if signed in.
     */
    fun isUserSignedIn(): Boolean {
        return isSignedIn
    }

    /**
     * Sign out.
     */
    fun signOut(activity: Activity) {
        if (isSignedIn) {
            try {
                PlayGames.getGamesClient(activity).signOut()
                isSignedIn = false
                Log.d(TAG, "Signed out of Play Games")
            } catch (e: Exception) {
                Log.w(TAG, "Sign-out error: ${e.message}")
            }
        }
    }
}
