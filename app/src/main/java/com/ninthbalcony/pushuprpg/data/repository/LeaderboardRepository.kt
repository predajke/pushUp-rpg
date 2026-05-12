package com.ninthbalcony.pushuprpg.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Public-facing leaderboard profile pushed to Firebase Realtime Database under
 * `leaderboard/{uid}`. WRITE-side only at this stage (Stage 1+2 of the online
 * leaderboard roadmap). Reads land in Stage 3 when LeaderboardScreen swaps its
 * mock roster for live data.
 *
 * RTDB schema for `leaderboard/{uid}`:
 * ```
 * name              : String
 * country           : String   (ISO-2)
 * clanTag           : String   (max 4 chars)
 * clanTagColor      : String   ("default" | "blue" | "green" | "red" | "yellow")
 * heroAvatar        : String
 * gender            : String   ("male" | "female")
 * level             : Int
 * prestige          : Int
 * power             : Int      (totalPower = base + equip + enchant + set/ach bonuses)
 * totalPushUps      : Int
 * totalTeethEarned  : Int
 * longestStreak     : Int
 * lastUpdated       : Long     (epoch millis)
 * ```
 *
 * Auth strategy is **Anonymous** for MVP. When Play Games arrives, the
 * anonymous user will be linked via `linkWithCredential()` so the uid is
 * preserved.
 */
class LeaderboardRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    /**
     * Make sure we have a Firebase user. Signs in anonymously if needed.
     * Idempotent — safe to call repeatedly. Returns null on failure.
     */
    suspend fun ensureSignedIn(): String? {
        auth.currentUser?.let { return it.uid }
        return runCatching {
            auth.signInAnonymously().await().user?.uid
        }.getOrElse {
            Log.w(TAG, "Anonymous sign-in failed: ${it.message}")
            null
        }
    }

    /**
     * Push the public profile snapshot to RTDB. No-op without an authenticated
     * user. Caller is responsible for debouncing — see [shouldPush].
     */
    suspend fun pushSnapshot(state: GameStateEntity) {
        val uid = ensureSignedIn() ?: return
        val payload = mapOf(
            "name"             to state.playerName,
            "country"          to state.playerCountry,
            "clanTag"          to state.clanTag,
            "clanTagColor"     to state.clanTagColor,
            "heroAvatar"       to state.heroAvatar,
            "gender"           to state.playerGender,
            "level"            to state.playerLevel,
            "prestige"         to state.prestigeLevel,
            "power"            to com.ninthbalcony.pushuprpg.utils.GameCalculations.calculateTotalStats(state).power,
            "totalPushUps"     to state.totalPushUpsAllTime,
            "totalTeethEarned" to state.totalTeethEarned,
            "longestStreak"    to state.longestStreak,
            "lastUpdated"      to System.currentTimeMillis(),
        )
        runCatching {
            database.reference.child("leaderboard").child(uid).updateChildren(payload).await()
            Log.d(TAG, "Pushed leaderboard snapshot for uid=$uid")
        }.onFailure { Log.w(TAG, "Push failed: ${it.message}") }
    }

    /**
     * Fingerprint of fields that are publicly visible on the leaderboard.
     * Used for `distinctUntilChangedBy` so we only fire RTDB writes when
     * something a player would actually see has changed.
     */
    fun publicFingerprint(state: GameStateEntity): Int = listOf(
        state.playerName,
        state.playerCountry,
        state.clanTag,
        state.clanTagColor,
        state.heroAvatar,
        state.playerGender,
        state.playerLevel,
        state.prestigeLevel,
        state.totalPushUpsAllTime,
        state.totalTeethEarned,
        state.longestStreak,
        // Power depends on equipment + base stat allocations + achievements.
        // Hashing the slots+base+achievements is cheaper than recomputing totalPower
        // here, and changes propagate through the same debounced push pipeline.
        state.basePower,
        state.equippedHead,
        state.equippedNecklace,
        state.equippedWeapon1,
        state.equippedWeapon2,
        state.equippedPants,
        state.equippedBoots,
        state.activeAchievementIds,
    ).hashCode()

    /**
     * Fetch top entries ordered by `totalPushUps` (descending).
     * RTDB returns ascending → we reverse client-side.
     * Returns empty list on failure.
     */
    suspend fun fetchTopByPushUps(limit: Int = 100): List<LeaderboardEntry> {
        ensureSignedIn() ?: return emptyList()
        val query: Query = database.reference
            .child("leaderboard")
            .orderByChild("totalPushUps")
            .limitToLast(limit)
        val cutoff = System.currentTimeMillis() - ZOMBIE_TTL_MS
        return runCatching {
            val snap = query.get().await()
            snap.children.mapNotNull { child ->
                val uid = child.key ?: return@mapNotNull null
                snapshotToEntry(uid, child)
            }.filter { it.lastUpdated == 0L || it.lastUpdated > cutoff }
             .sortedByDescending { it.totalPushUps }
        }.getOrElse {
            Log.w(TAG, "Fetch top failed: ${it.message}")
            emptyList()
        }
    }

    // ── Friend codes & friend list ──────────────────────────────────────────

    /** uid of the currently authenticated user (or null). */
    fun currentUid(): String? = auth.currentUser?.uid

    /**
     * Ensure the current user has a 6-char friend code. Returns the existing
     * or freshly-claimed code. Uses RTDB rule + setValue for atomic claim:
     * the rule rejects if `friendCodes/{code}` belongs to another uid.
     */
    suspend fun ensureFriendCode(): String? {
        val uid = ensureSignedIn() ?: return null
        // Look up existing code in our public profile
        val existing = runCatching {
            database.reference.child("leaderboard").child(uid).child("friendCode")
                .get().await().getValue(String::class.java)
        }.getOrNull()
        if (!existing.isNullOrBlank()) return existing

        // Try to claim a fresh code (collisions are vanishingly rare with 32^6 = 1B combos)
        repeat(5) {
            val code = randomFriendCode()
            val claimed = runCatching {
                database.reference.child("friendCodes").child(code)
                    .setValue(uid).await()
                true
            }.getOrElse {
                Log.d(TAG, "Code $code claim collision: ${it.message}")
                false
            }
            if (claimed) {
                runCatching {
                    database.reference.child("leaderboard").child(uid).child("friendCode")
                        .setValue(code).await()
                }
                Log.d(TAG, "Friend code claimed: $code")
                return code
            }
        }
        return null
    }

    /**
     * Translate a 6-char code → uid. Returns null if not found or input invalid.
     */
    suspend fun lookupFriendCode(code: String): String? {
        ensureSignedIn() ?: return null
        val clean = code.uppercase().filter { it.isLetterOrDigit() }
        if (clean.length != 6) return null
        return runCatching {
            database.reference.child("friendCodes").child(clean).get().await()
                .getValue(String::class.java)
        }.getOrNull()
    }

    /** Add friendUid to my friends list. Returns true on success. */
    suspend fun addFriend(friendUid: String): Boolean {
        val myUid = ensureSignedIn() ?: return false
        if (friendUid == myUid) return false
        return runCatching {
            database.reference.child("friends").child(myUid).child(friendUid)
                .setValue(System.currentTimeMillis()).await()
            true
        }.getOrElse {
            Log.w(TAG, "Add friend failed: ${it.message}")
            false
        }
    }

    /** Remove friend from my list. Returns true on success. */
    suspend fun removeFriend(friendUid: String): Boolean {
        val myUid = ensureSignedIn() ?: return false
        return runCatching {
            database.reference.child("friends").child(myUid).child(friendUid)
                .removeValue().await()
            true
        }.getOrElse { false }
    }

    /**
     * Real-time flow of the caller's friends list. Subscribes to
     * `friends/{myUid}` with a persistent listener; whenever the uid-set
     * changes (add / remove on any device), it fans-out to fetch each
     * friend's public profile and emits the updated list.
     *
     * Collect this in the ViewModel's `viewModelScope`; it auto-cancels on
     * ViewModel destruction.
     */
    fun friendsFlow(myUid: String): Flow<List<LeaderboardEntry>> = callbackFlow {
        val scope = this   // capture ProducerScope so the inner class can launch coroutines
        val ref = database.reference.child("friends").child(myUid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friendIds = snapshot.children.mapNotNull { it.key }
                scope.launch {
                    val entries = friendIds.mapNotNull { fid ->
                        runCatching {
                            val s = database.reference.child("leaderboard").child(fid).get().await()
                            if (s.exists()) snapshotToEntry(fid, s) else null
                        }.getOrNull()
                    }
                    trySend(entries)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "friendsFlow cancelled: ${error.message}")
                trySend(emptyList())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private fun snapshotToEntry(uid: String, s: DataSnapshot) = LeaderboardEntry(
        uid              = uid,
        name             = s.child("name").getValue(String::class.java) ?: "Player",
        country          = s.child("country").getValue(String::class.java) ?: "",
        clanTag          = s.child("clanTag").getValue(String::class.java) ?: "",
        clanTagColor     = s.child("clanTagColor").getValue(String::class.java) ?: "default",
        heroAvatar       = s.child("heroAvatar").getValue(String::class.java) ?: "avatar_base",
        gender           = s.child("gender").getValue(String::class.java) ?: "male",
        level            = s.child("level").getValue(Int::class.java) ?: 1,
        prestige         = s.child("prestige").getValue(Int::class.java) ?: 0,
        power            = s.child("power").getValue(Int::class.java) ?: 0,
        totalPushUps     = s.child("totalPushUps").getValue(Int::class.java) ?: 0,
        totalTeethEarned = s.child("totalTeethEarned").getValue(Int::class.java) ?: 0,
        longestStreak    = s.child("longestStreak").getValue(Int::class.java) ?: 0,
        lastUpdated      = s.child("lastUpdated").getValue(Long::class.java) ?: 0L,
        friendCode       = s.child("friendCode").getValue(String::class.java) ?: "",
    )

    private fun randomFriendCode(length: Int = 6): String {
        // Excludes 0/O, 1/I/L for readability
        val alphabet = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
        return (1..length).map { alphabet.random() }.joinToString("")
    }

    /**
     * Reactive Firebase auth uid. Emits null when signed out, and the new
     * uid whenever it changes — critically, this fires after Play Games
     * sign-in completes and the anonymous Firebase account is upgraded to
     * (or replaced by) the Play Games-linked uid.
     *
     * Caller pattern: `collectLatest { uid -> refetchPerUidData(uid) }`,
     * so previous-uid coroutines (friends, friend code) auto-cancel and
     * restart under the new uid. Without this, fresh installs land on the
     * old anonymous uid and never see their existing friend code.
     */
    fun authUidFlow(): Flow<String?> = callbackFlow {
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { fa ->
            trySend(fa.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Live connection state from Firebase's special `.info/connected` virtual
     * node. Emits true when the SDK has an active socket to the backend, false
     * otherwise. Survives auth churn and network blips.
     */
    fun connectionStateFlow(): Flow<Boolean> = callbackFlow {
        val ref = database.getReference(".info/connected")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Boolean::class.java) == true)
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(false)
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    companion object {
        private const val TAG = "LeaderboardRepo"
        /** Minimum interval between RTDB writes per device. */
        const val PUSH_DEBOUNCE_MS = 60_000L
        /** Cache TTL for fetched leaderboard data. */
        const val CACHE_TTL_MS = 5 * 60_000L
        /** Entries not updated within this window are treated as zombie accounts and hidden. */
        const val ZOMBIE_TTL_MS = 30L * 24 * 3600 * 1000  // 30 days
    }
}

/**
 * Read-side projection of `leaderboard/{uid}`. Mirrors the RTDB schema
 * declared in [LeaderboardRepository].
 */
data class LeaderboardEntry(
    val uid: String,
    val name: String,
    val country: String,
    val clanTag: String,
    val clanTagColor: String,
    val heroAvatar: String,
    val gender: String,
    val level: Int,
    val prestige: Int,
    val power: Int = 0,
    val totalPushUps: Int,
    val totalTeethEarned: Int,
    val longestStreak: Int,
    val lastUpdated: Long,
    val friendCode: String = "",
)
