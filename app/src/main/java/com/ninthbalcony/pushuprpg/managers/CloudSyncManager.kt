package com.ninthbalcony.pushuprpg.managers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import kotlinx.coroutines.*

class CloudSyncManager(private val context: Context, private val scope: CoroutineScope) {
    companion object {
        private const val TAG = "CloudSyncManager"
        private const val SYNC_INTERVAL_MS = 3600000L
        private const val FIREBASE_USERS_PATH = "users"
    }

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var syncJob: Job? = null
    private var isNetworkAvailable = false

    init {
        setupNetworkListener()
    }

    fun initialize(onSyncComplete: ((Boolean) -> Unit)? = null) {
        Log.d(TAG, "CloudSyncManager initialized")
        scope.launch {
            val success = performSync()
            onSyncComplete?.invoke(success)
            startPeriodicSync()
        }
    }

    suspend fun syncNow(): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Manual sync triggered")
        performSync()
    }

    fun uploadGameStateAsync(gameState: GameStateEntity, userId: String? = null) {
        scope.launch(Dispatchers.IO) {
            val currentUserId = userId ?: getCurrentUserId()
            if (currentUserId.isEmpty()) {
                Log.w(TAG, "User not signed in, skipping upload")
                return@launch
            }
            if (!isNetworkAvailable) {
                Log.d(TAG, "No internet, skipping upload")
                return@launch
            }

            val userPath = "$FIREBASE_USERS_PATH/$currentUserId"
            val updates = mapOf(
                "$userPath/stats/totalPushups" to gameState.totalPushUpsAllTime,
                "$userPath/stats/playerLevel" to gameState.playerLevel,
                "$userPath/stats/currentHp" to gameState.currentHp,
                "$userPath/stats/teeth" to gameState.teeth,
                "$userPath/stats/lastSaveTime" to System.currentTimeMillis(),
                "$userPath/achievements" to gameState.achievementsJson,
                "$userPath/inventory" to gameState.inventoryItems,
                "$userPath/lastSyncTime" to System.currentTimeMillis()
            )

            database.reference.updateChildren(updates)
                .addOnSuccessListener { Log.d(TAG, "Upload successful") }
                .addOnFailureListener { e -> Log.w(TAG, "Upload failed: ${e.message}") }
        }
    }

    suspend fun signInSilent(): String = withContext(Dispatchers.IO) {
        try {
            auth.currentUser?.uid ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Sign in error: ${e.message}")
            ""
        }
    }

    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""
    fun isUserSignedIn(): Boolean = auth.currentUser != null

    private fun setupNetworkListener() {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                Log.d(TAG, "Internet available")
                isNetworkAvailable = true
                scope.launch { performSync() }
            }
            override fun onLost(network: android.net.Network) {
                Log.d(TAG, "Internet lost")
                isNetworkAvailable = false
            }
        }

        try {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
            isNetworkAvailable = isInternetAvailable()
        } catch (e: Exception) {
            Log.e(TAG, "Network listener setup failed: ${e.message}")
        }
    }

    private fun isInternetAvailable(): Boolean {
        try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            return false
        }
    }

    private suspend fun performSync(): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting sync...")
        if (!isNetworkAvailable) {
            Log.d(TAG, "No internet, skipping sync")
            return@withContext false
        }
        if (getCurrentUserId().isEmpty()) {
            Log.d(TAG, "User not signed in, skipping sync")
            return@withContext false
        }
        return@withContext true
    }

    private fun startPeriodicSync() {
        syncJob?.cancel()
        syncJob = scope.launch {
            while (isActive) {
                delay(SYNC_INTERVAL_MS)
                if (isNetworkAvailable) {
                    performSync()
                }
            }
        }
    }

    fun destroy() {
        syncJob?.cancel()
        Log.d(TAG, "CloudSyncManager destroyed")
    }
}
