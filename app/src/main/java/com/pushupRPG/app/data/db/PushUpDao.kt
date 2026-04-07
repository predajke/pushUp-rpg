package com.pushupRPG.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PushUpDao {

    // --- GameState ---
    @Query("SELECT * FROM game_state WHERE id = 1")
    suspend fun getGameState(): GameStateEntity?

    @Query("SELECT * FROM game_state WHERE id = 1")
    fun getGameStateFlow(): Flow<GameStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameState(state: GameStateEntity)

    // --- PushUp Records ---
    @Insert
    suspend fun insertPushUpRecord(record: PushUpRecordEntity)

    @Query("SELECT SUM(count) FROM pushup_records WHERE date = :date")
    suspend fun getPushUpsForDate(date: String): Int?

    @Query("SELECT date, SUM(count) as count FROM pushup_records GROUP BY date ORDER BY date DESC LIMIT 7")
    suspend fun getLast7DaysStats(): List<DayStats>

    @Query("SELECT SUM(count) FROM pushup_records WHERE date >= :startDate")
    suspend fun getPushUpsSince(startDate: String): Int?

    @Query("SELECT SUM(count) FROM pushup_records")
    suspend fun getTotalPushUps(): Int?

    // --- Log Entries ---
    @Insert
    suspend fun insertLog(log: LogEntryEntity)

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<LogEntryEntity>>

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<LogEntryEntity>>

    @Query("DELETE FROM log_entries WHERE timestamp < :timestamp")
    suspend fun deleteOldLogs(timestamp: Long)

    @Query("DELETE FROM log_entries")
    suspend fun deleteAllLogs()

    @Query("DELETE FROM pushup_records")
    suspend fun deleteAllPushUpRecords()
}

data class DayStats(
    val date: String,
    val count: Int
)