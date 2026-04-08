package com.pushupRPG.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pushupRPG.app.data.db.entity.MaxPushUpsAttemptEntity

@Dao
interface MaxPushUpsDao {
    @Insert
    suspend fun insertAttempt(attempt: MaxPushUpsAttemptEntity)

    @Query("SELECT COUNT(*) FROM max_pushups_attempts WHERE date = :date")
    suspend fun getAttemptsCountForDate(date: String): Int

    @Query("SELECT * FROM max_pushups_attempts WHERE date = :date ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastAttemptForDate(date: String): MaxPushUpsAttemptEntity?

    @Query("SELECT * FROM max_pushups_attempts ORDER BY timestamp DESC LIMIT 100")
    suspend fun getAllAttempts(): List<MaxPushUpsAttemptEntity>
}
