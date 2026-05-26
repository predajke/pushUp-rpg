package com.ninthbalcony.pushuprpg.managers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.PlayGamesAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.ref.WeakReference

/**
 * Wrapper around Play Games SDK v2 + Firebase Auth.
 *
 * Sign-in flow:
 *   1. PlayGames.getGamesSignInClient(activity).signIn() — opens Play Games consent UI if needed
 *   2. requestServerSideAccess(WEB_CLIENT_ID, false) — returns a one-shot serverAuthCode
 *   3. PlayGamesAuthProvider.getCredential(serverAuthCode) → Firebase credential
 *   4. linkWithCredential() (anonymous → Play Games) or signInWithCredential() (collision recovery)
 *
 * The Web (Game Server) OAuth client whose ID is below must be configured in
 * Firebase Auth → Play Games provider for token verification to work.
 */
class PlayGamesManager(private val context: Context, private val scope: CoroutineScope) {

    companion object {
        private const val TAG = "PlayGamesManager"

        // Web (Game Server) OAuth client ID. Public — safe to ship in source.
        private const val WEB_CLIENT_ID =
            "477535266151-buoltg1ea91rc772ulcqvquv5f175q19.apps.googleusercontent.com"

        // Achievement IDs (from Play Console → Play Games Services → Achievements)
        private const val ACHIEVEMENT_FIRST_FIGHT = "CgkI58rH-vINEAIQAA"
        private const val ACHIEVEMENT_MASTER_PUSHUPS = "CgkI58rH-vINEAIQAQ"
        private const val ACHIEVEMENT_EPIC_CATCH = "CgkI58rH-vINEAIQAg"
        private const val ACHIEVEMENT_LEGENDARY_CATCH = "CgkI58rH-vINEAIQAw"
        private const val ACHIEVEMENT_RICH = "CgkI58rH-vINEAIQBA"
        private const val ACHIEVEMENT_FAILED_ENCHANTS = "CgkI58rH-vINEAIQBQ"
        private const val ACHIEVEMENT_FULL_WARDROBE = "CgkI58rH-vINEAIQBg"
        private const val ACHIEVEMENT_ALCHEMIST = "CgkI58rH-vINEAIQBw"
    }

    private val auth = FirebaseAuth.getInstance()
    private var activityRef: WeakReference<Activity>? = null

    // Reactive sign-in state — observers (e.g. GameViewModel) get the silent
    // sign-in result automatically when the async callback fires, eliminating
    // the one-shot read race that left Profile showing "not connected".
    private val _isAuth = MutableStateFlow(false)
    val isAuthFlow: StateFlow<Boolean> = _isAuth.asStateFlow()

    private val _displayName = MutableStateFlow("Player")
    val displayNameFlow: StateFlow<String> = _displayName.asStateFlow()

    private val _iconUri = MutableStateFlow<String?>(null)
    val iconUri: StateFlow<String?> = _iconUri.asStateFlow()

    init {
        // SDK init must happen exactly once per process. Idempotent in v2.
        try {
            PlayGamesSdk.initialize(context)
        } catch (e: Throwable) {
            Log.w(TAG, "PlayGamesSdk.initialize threw (may be already initialized): ${e.message}")
        }
    }

    // ── Sign in / out ────────────────────────────────────────────────────────

    /**
     * Silent sign-in. No UI. Sets [isSignedIn] to true if Play Games is already
     * authenticated on this device. Also links Firebase uid → Play Games on
     * success, so a fresh anonymous user (created on reinstall) is upgraded
     * before any RTDB pushes happen.
     */
    fun signIn(activity: Activity, onResult: (Boolean) -> Unit) {
        activityRef = WeakReference(activity)
        val client = PlayGames.getGamesSignInClient(activity)
        client.isAuthenticated.addOnCompleteListener { task ->
            val ok = task.isSuccessful && task.result?.isAuthenticated == true
            _isAuth.value = ok
            if (ok) {
                fetchPlayerName(activity)
                Log.d(TAG, "Silent sign-in OK")
                scope.launch(Dispatchers.IO) {
                    linkFirebaseWithPlayGames(activity)
                }
            } else {
                Log.d(TAG, "Silent sign-in unavailable: ${task.exception?.message}")
            }
            onResult(ok)
        }
    }

    /**
     * Interactive sign-in. Opens the Play Games consent dialog if the user
     * hasn't authorized this game before. After success, links the local
     * anonymous Firebase user with the Play Games credential.
     *
     * @return true if signed in AND Firebase linking succeeded (or fell back
     *         to signInWithCredential on collision).
     */
    suspend fun signInInteractive(activity: Activity): Boolean {
        activityRef = WeakReference(activity)
        return try {
            val client = PlayGames.getGamesSignInClient(activity)
            val authResult = client.signIn().await()
            if (!authResult.isAuthenticated) {
                Log.w(TAG, "Sign-in returned not authenticated")
                return false
            }
            _isAuth.value = true
            fetchPlayerName(activity)
            linkFirebaseWithPlayGames(activity)
            true
        } catch (e: Throwable) {
            Log.w(TAG, "Sign-in failed: ${e.message}")
            false
        }
    }

    /**
     * Get a serverAuthCode from Play Games and use it as a Firebase credential.
     * Upgrades the anonymous account, or signs into the existing Play Games
     * uid on collision.
     */
    private suspend fun linkFirebaseWithPlayGames(activity: Activity) {
        try {
            val current = auth.currentUser
            val alreadyLinked = current?.providerData?.any { it.providerId == "playgames.google.com" } == true
            if (alreadyLinked) {
                Log.d(TAG, "Already linked to Play Games (uid=${current?.uid}) — skipping")
                return
            }

            val client = PlayGames.getGamesSignInClient(activity)
            Log.d(TAG, "Requesting serverAuthCode with WEB_CLIENT_ID...")
            val serverAuthCode = client.requestServerSideAccess(WEB_CLIENT_ID, /* forceRefresh */ false).await()
            Log.d(TAG, "Got serverAuthCode (length=${serverAuthCode.length})")
            val credential = PlayGamesAuthProvider.getCredential(serverAuthCode)
            Log.d(TAG, "Current Firebase user: uid=${current?.uid}, isAnonymous=${current?.isAnonymous}")
            if (current != null && current.isAnonymous) {
                try {
                    current.linkWithCredential(credential).await()
                    Log.d(TAG, "Linked anonymous → Play Games OK (uid=${auth.currentUser?.uid})")
                } catch (collision: FirebaseAuthUserCollisionException) {
                    val oldUid = current.uid
                    val result = auth.signInWithCredential(credential).await()
                    Log.d(TAG, "Collision — switched from anonymous uid=$oldUid to Play Games uid=${result.user?.uid}")
                }
            } else {
                val result = auth.signInWithCredential(credential).await()
                Log.d(TAG, "Signed in with Play Games credential, uid=${result.user?.uid}")
            }
            Log.d(TAG, "Firebase link complete — final uid=${auth.currentUser?.uid}, providers=${auth.currentUser?.providerData?.map { it.providerId }}")
        } catch (e: Throwable) {
            Log.e(TAG, "Firebase link FAILED: ${e.javaClass.simpleName}: ${e.message}", e)
        }
    }

    /**
     * Sign out from Firebase. Note: Play Games SDK v2 has no signOut — sign-in
     * state is owned by the OS and persists across app restarts. We can only
     * detach our Firebase identity.
     */
    fun signOut(@Suppress("UNUSED_PARAMETER") activity: Activity? = null) {
        auth.signOut()
        _isAuth.value = false
        _displayName.value = "Player"
        _iconUri.value = null
        Log.d(TAG, "Firebase signed out (Play Games session managed by OS)")
    }

    private fun fetchPlayerName(activity: Activity) {
        PlayGames.getPlayersClient(activity).currentPlayer.addOnCompleteListener { t ->
            if (t.isSuccessful) {
                val player = t.result
                _displayName.value = player?.displayName ?: "Player"
                _iconUri.value = player?.iconImageUrl ?: player?.iconImageUri?.toString()
                Log.d(TAG, "Player display name: ${_displayName.value}, icon: ${_iconUri.value}")
            } else {
                Log.w(TAG, "Failed to fetch player name: ${t.exception?.message}")
            }
        }
    }

    // ── Achievements (v2 API requires Activity, cached weakly from sign-in) ──

    private fun act(): Activity? = activityRef?.get()

    fun unlockAchievementFirstFight() {
        if (!_isAuth.value) return
        val a = act() ?: return
        PlayGames.getAchievementsClient(a).unlock(ACHIEVEMENT_FIRST_FIGHT)
        Log.d(TAG, "Unlocked: First Fight")
    }

    fun incrementAchievementMasterPushups(steps: Int) {
        if (!_isAuth.value) return
        val a = act() ?: return
        PlayGames.getAchievementsClient(a).increment(ACHIEVEMENT_MASTER_PUSHUPS, steps)
        Log.d(TAG, "Incremented Master Pushups by $steps")
    }

    fun revealAchievementMasterPushups() {
        if (!_isAuth.value) return
        val a = act() ?: return
        PlayGames.getAchievementsClient(a).reveal(ACHIEVEMENT_MASTER_PUSHUPS)
        Log.d(TAG, "Revealed: Master Pushups")
    }

    fun unlockAchievementEpicCatch() {
        if (!_isAuth.value) return
        val a = act() ?: return
        PlayGames.getAchievementsClient(a).unlock(ACHIEVEMENT_EPIC_CATCH)
        Log.d(TAG, "Unlocked: Epic Catch")
    }

    fun unlockAchievementLegendaryCatch() {
        if (!_isAuth.value) return
        val a = act() ?: return
        PlayGames.getAchievementsClient(a).unlock(ACHIEVEMENT_LEGENDARY_CATCH)
        Log.d(TAG, "Unlocked: Legendary Catch")
    }

    fun unlockAchievementRich() {
        if (!_isAuth.value) return
        val a = act() ?: return
        PlayGames.getAchievementsClient(a).unlock(ACHIEVEMENT_RICH)
        Log.d(TAG, "Unlocked: Rich")
    }

    fun incrementAchievementFailedEnchants(steps: Int) {
        if (!_isAuth.value) return
        val a = act() ?: return
        PlayGames.getAchievementsClient(a).increment(ACHIEVEMENT_FAILED_ENCHANTS, steps)
        Log.d(TAG, "Incremented Failed Enchants by $steps")
    }

    fun unlockAchievementFullWardrobe() {
        if (!_isAuth.value) return
        val a = act() ?: return
        PlayGames.getAchievementsClient(a).unlock(ACHIEVEMENT_FULL_WARDROBE)
        Log.d(TAG, "Unlocked: Full Wardrobe")
    }

    fun incrementAchievementAlchemist(steps: Int) {
        if (!_isAuth.value) return
        val a = act() ?: return
        PlayGames.getAchievementsClient(a).increment(ACHIEVEMENT_ALCHEMIST, steps)
        Log.d(TAG, "Incremented Alchemist by $steps")
    }

    fun destroy() {
        activityRef = null
        _isAuth.value = false
        Log.d(TAG, "PlayGamesManager destroyed")
    }
}
