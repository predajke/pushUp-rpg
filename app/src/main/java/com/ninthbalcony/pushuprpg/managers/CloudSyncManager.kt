package com.ninthbalcony.pushuprpg.managers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Облачное сохранение игрового стейта через Firebase RTDB.
 *
 * Схема: `users/{uid}/state` хранит весь [GameStateEntity] как JSON-blob.
 * Поле [GameStateEntity.lastUpdated] используется для conflict resolution —
 * побеждает версия с большим timestamp.
 *
 * Lifecycle:
 *  1. App start → MainActivity вызывает [tryRestoreOnLaunch] после
 *     завершения Play Games sign-in callback. Если облако новее локального,
 *     возвращает облачный state, эмитит [RestoreResult.Restored] в [restoreEvent].
 *  2. [restoreComplete] переходит в `true` (вне зависимости от того,
 *     применилось ли облако). ViewModel ждёт этого флага, прежде чем
 *     начинать upload pipeline — иначе старт затёр бы облако дефолтным Hero.
 *  3. ViewModel debounced-пайплайн вызывает [uploadGameStateAsync] при каждом
 *     изменении state (с 30-сек задержкой).
 *
 * Замечания:
 *  - [GameStateEntity.lastBattleTick] на restore сбрасывается в `now` —
 *    иначе auto-battle отыграет накопленные тики от предыдущего устройства.
 *  - Без интернета restore и upload — no-op, локальная игра не блокируется.
 *  - Без auth (Play Games не включён, anonymous не успел) restore возвращает null,
 *    приложение продолжает с локальным state.
 */
class CloudSyncManager(context: Context, private val scope: CoroutineScope) {

    companion object {
        private const val TAG = "CloudSyncManager"
        private const val FIREBASE_USERS_PATH = "users"
        private const val STATE_KEY = "state"
        /** Гистерезис против часовых дрейфов между устройствами. */
        private const val GRACE_MS = 5_000L
        /** Максимум ждём появления Firebase uid (включая Play Games link). */
        private const val AUTH_WAIT_TIMEOUT_MS = 4_000L
    }

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val gson = Gson()
    @Volatile private var isNetworkAvailable = false

    /** true когда восстановление завершено (успех ИЛИ no-op). Upload pipeline должен ждать. */
    private val _restoreComplete = MutableStateFlow(false)
    val restoreComplete: StateFlow<Boolean> = _restoreComplete.asStateFlow()

    /** Эмитится один раз если restore реально применил облачный стейт. UI слушает для Toast. */
    private val _restoreEvent = MutableSharedFlow<RestoreResult>(replay = 0, extraBufferCapacity = 1)
    val restoreEvent: SharedFlow<RestoreResult> = _restoreEvent.asSharedFlow()

    init {
        setupNetworkListener()
    }

    /**
     * Один раз на старте: ждём стабильного auth uid, читаем `users/{uid}/state`,
     * парсим JSON в [GameStateEntity], сравниваем `lastUpdated` с `local`.
     *
     * @return облачный state с поправкой `lastBattleTick = now` если облако
     *   новее [local] минимум на [GRACE_MS]; иначе null.
     *
     * Всегда выставляет [restoreComplete] = true перед возвратом.
     */
    suspend fun tryRestoreOnLaunch(local: GameStateEntity): GameStateEntity? =
        withContext(Dispatchers.IO) {
            try {
                if (!isNetworkAvailable) {
                    Log.d(TAG, "Restore skipped: offline")
                    return@withContext null
                }
                val uid = waitForAuth()
                if (uid.isNullOrEmpty()) {
                    Log.d(TAG, "Restore skipped: no auth")
                    return@withContext null
                }
                val cloudJson = runCatching {
                    database.reference
                        .child(FIREBASE_USERS_PATH).child(uid).child(STATE_KEY)
                        .get().await().getValue(String::class.java)
                }.getOrElse {
                    Log.w(TAG, "Cloud read failed: ${it.message}")
                    null
                }
                if (cloudJson.isNullOrBlank()) {
                    Log.d(TAG, "Restore skipped: no cloud snapshot for uid=$uid")
                    return@withContext null
                }
                val cloud = runCatching { gson.fromJson(cloudJson, GameStateEntity::class.java) }
                    .getOrElse {
                        Log.w(TAG, "Cloud JSON parse failed: ${it.message}")
                        return@withContext null
                    }
                if (cloud.lastUpdated <= local.lastUpdated + GRACE_MS) {
                    Log.d(TAG, "Cloud not newer (cloud=${cloud.lastUpdated}, local=${local.lastUpdated})")
                    return@withContext null
                }
                Log.d(TAG, "Restoring cloud state: level=${cloud.playerLevel}, pushups=${cloud.totalPushUpsAllTime}")
                _restoreEvent.tryEmit(RestoreResult.Restored(cloud.playerLevel, cloud.totalPushUpsAllTime))
                // Сбрасываем сессионные поля, чтобы не отыграть тики с предыдущего устройства.
                cloud.copy(lastBattleTick = System.currentTimeMillis())
            } finally {
                _restoreComplete.value = true
            }
        }

    /** Сериализует state в JSON и пушит в `users/{uid}/state`. Вызывается из debounced pipeline. */
    fun uploadGameStateAsync(state: GameStateEntity) {
        scope.launch(Dispatchers.IO) {
            val uid = auth.currentUser?.uid
            if (uid.isNullOrEmpty()) { Log.d(TAG, "Upload skipped: no auth"); return@launch }
            if (!isNetworkAvailable) { Log.d(TAG, "Upload skipped: offline"); return@launch }
            val json = runCatching { gson.toJson(state) }.getOrNull() ?: run {
                Log.w(TAG, "Serialize failed"); return@launch
            }
            runCatching {
                database.reference
                    .child(FIREBASE_USERS_PATH).child(uid).child(STATE_KEY)
                    .setValue(json).await()
                Log.d(TAG, "Uploaded snapshot (${json.length}B) for uid=$uid")
            }.onFailure { Log.w(TAG, "Upload failed: ${it.message}") }
        }
    }

    /** Опрашиваем auth.currentUser?.uid до [AUTH_WAIT_TIMEOUT_MS] — он может смениться после Play Games link. */
    private suspend fun waitForAuth(): String? {
        val deadline = System.currentTimeMillis() + AUTH_WAIT_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            val uid = auth.currentUser?.uid
            if (!uid.isNullOrEmpty()) return uid
            delay(100)
        }
        return auth.currentUser?.uid
    }

    private fun setupNetworkListener() {
        try {
            connectivityManager.registerDefaultNetworkCallback(
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: android.net.Network) { isNetworkAvailable = true }
                    override fun onLost(network: android.net.Network) { isNetworkAvailable = false }
                }
            )
            isNetworkAvailable = isInternetAvailable()
        } catch (e: Exception) {
            Log.e(TAG, "Network listener setup failed: ${e.message}")
        }
    }

    private fun isInternetAvailable(): Boolean = try {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } catch (e: Exception) { false }

    fun destroy() { Log.d(TAG, "CloudSyncManager destroyed") }
}

/** Результат [CloudSyncManager.tryRestoreOnLaunch] для UI-уведомлений. */
sealed class RestoreResult {
    data class Restored(val level: Int, val totalPushUps: Int) : RestoreResult()
}
