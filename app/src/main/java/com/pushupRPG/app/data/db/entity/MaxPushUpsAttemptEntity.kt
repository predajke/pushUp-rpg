package com.pushupRPG.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "max_pushups_attempts")
data class MaxPushUpsAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,                           // "yyyy-MM-dd" UTC
    val timestamp: Long = System.currentTimeMillis(),
    val attemptNumber: Int                      // 1, 2, 3, ... (какой по счету в этот день)
)
