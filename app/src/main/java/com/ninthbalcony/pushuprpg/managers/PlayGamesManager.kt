package com.ninthbalcony.pushuprpg.managers

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.games.Games
import com.google.android.gms.games.GamesClient
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.*

class PlayGamesManager(private val context: Context) {
    companion object {
        private const val TAG = "PlayGamesManager"

        // Achievement IDs
        private const val ACHIEVEMENT_FIRST_FIGHT = "CgkI58rH-vINEAIQAA"
        private const val ACHIEVEMENT_MASTER_PUSHUPS = "CgkI58rH-vINEAIQAQ"
        private const val ACHIEVEMENT_EPIC_CATCH = "CgkI58rH-vINEAIQAg"
        private const val ACHIEVEMENT_LEGENDARY_CATCH = "CgkI58rH-vINEAIQAw"
        private const val ACHIEVEMENT_RICH = "CgkI58rH-vINEAIQBA"
        private const val ACHIEVEMENT_FAILED_ENCHANTS = "CgkI58rH-vINEAIQBQ"
        private const val ACHIEVEMENT_FULL_WARDROBE = "CgkI58rH-vINEAIQBg"
        private const val ACHIEVEMENT_ALCHEMIST = "CgkI58rH-vINEAIQBw"
    }

    private var gamesClient: GamesClient? = null
    private var signInAccount: GoogleSignInAccount? = null

    fun signIn(activity: android.app.Activity, onResult: (Boolean) -> Unit) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestEmail()
            .build()

        val signInClient = GoogleSignIn.getClient(context, gso)

        signInClient.silentSignIn().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                signInAccount = task.result
                gamesClient = Games.getGamesClient(context, signInAccount!!)
                Log.d(TAG, "Silent sign-in successful: ${signInAccount?.displayName}")
                onResult(true)
            } else {
                Log.w(TAG, "Silent sign-in failed: ${task.exception?.message}")
                onResult(false)
            }
        }
    }

    fun signOut(activity: android.app.Activity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestEmail()
            .build()

        val signInClient = GoogleSignIn.getClient(context, gso)
        signInClient.signOut().addOnCompleteListener {
            signInAccount = null
            gamesClient = null
            Log.d(TAG, "Signed out from Play Games")
        }
    }

    fun unlockAchievementFirstFight() {
        if (gamesClient == null) {
            Log.w(TAG, "Games client not initialized - cannot unlock achievement")
            return
        }

        Games.getAchievementsClient(context, signInAccount!!).unlock(ACHIEVEMENT_FIRST_FIGHT)
        Log.d(TAG, "Unlocked achievement: First Fight")
    }

    fun incrementAchievementMasterPushups(steps: Int) {
        if (gamesClient == null) {
            Log.w(TAG, "Games client not initialized - cannot increment achievement")
            return
        }

        Games.getAchievementsClient(context, signInAccount!!).increment(ACHIEVEMENT_MASTER_PUSHUPS, steps)
        Log.d(TAG, "Incremented Master Pushups achievement by $steps")
    }

    fun revealAchievementMasterPushups() {
        if (gamesClient == null) {
            Log.w(TAG, "Games client not initialized - cannot reveal achievement")
            return
        }

        Games.getAchievementsClient(context, signInAccount!!).reveal(ACHIEVEMENT_MASTER_PUSHUPS)
        Log.d(TAG, "Revealed achievement: Master Pushups")
    }

    fun unlockAchievementEpicCatch() {
        if (gamesClient == null) return
        Games.getAchievementsClient(context, signInAccount!!).unlock(ACHIEVEMENT_EPIC_CATCH)
        Log.d(TAG, "Unlocked achievement: Epic Catch")
    }

    fun unlockAchievementLegendaryCatch() {
        if (gamesClient == null) return
        Games.getAchievementsClient(context, signInAccount!!).unlock(ACHIEVEMENT_LEGENDARY_CATCH)
        Log.d(TAG, "Unlocked achievement: Legendary Catch")
    }

    fun unlockAchievementRich() {
        if (gamesClient == null) return
        Games.getAchievementsClient(context, signInAccount!!).unlock(ACHIEVEMENT_RICH)
        Log.d(TAG, "Unlocked achievement: Rich")
    }

    fun incrementAchievementFailedEnchants(steps: Int) {
        if (gamesClient == null) return
        Games.getAchievementsClient(context, signInAccount!!).increment(ACHIEVEMENT_FAILED_ENCHANTS, steps)
        Log.d(TAG, "Incremented Failed Enchants by $steps")
    }

    fun unlockAchievementFullWardrobe() {
        if (gamesClient == null) return
        Games.getAchievementsClient(context, signInAccount!!).unlock(ACHIEVEMENT_FULL_WARDROBE)
        Log.d(TAG, "Unlocked achievement: Full Wardrobe")
    }

    fun incrementAchievementAlchemist(steps: Int) {
        if (gamesClient == null) return
        Games.getAchievementsClient(context, signInAccount!!).increment(ACHIEVEMENT_ALCHEMIST, steps)
        Log.d(TAG, "Incremented Alchemist by $steps")
    }

    fun getAchievementsClient(): GamesClient? {
        return gamesClient
    }

    fun isSignedIn(): Boolean {
        return signInAccount != null && gamesClient != null
    }

    fun getPlayerName(): String {
        return signInAccount?.displayName ?: "Player"
    }

    fun destroy() {
        gamesClient = null
        signInAccount = null
        Log.d(TAG, "PlayGamesManager destroyed")
    }
}
