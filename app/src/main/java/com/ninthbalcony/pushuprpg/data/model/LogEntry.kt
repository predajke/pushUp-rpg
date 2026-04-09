package com.ninthbalcony.pushuprpg.data.model

data class LogEntry(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val messageRu: String
)