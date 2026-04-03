package com.pushupRPG.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushupRPG.app.data.db.LogEntryEntity
import com.pushupRPG.app.ui.theme.*
// ИСПРАВЛЕНО 1: Обновлен путь к GameViewModel
import com.pushupRPG.app.ui.GameViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    // ИСПРАВЛЕНО 2: Добавлено initial = null
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val language = gameState?.language ?: "en"

    // ИСПРАВЛЕНО 3: Используем переменную allLogs вместо функции getAllLogs()
    val allLogs by viewModel.allLogs.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // --- Топбар ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = if (language == "ru") "Журнал боя" else "Battle Log",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // --- Список логов ---
        if (allLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (language == "ru")
                        "Событий пока нет.\nНачни тренировку!"
                    else
                        "No events yet.\nStart training!",
                    fontSize = 16.sp,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(allLogs) { log ->
                    LogItem(
                        log = log,
                        language = language
                    )
                }
            }
        }
    }
}

@Composable
fun LogItem(
    log: LogEntryEntity,
    language: String
) {
    val message = if (language == "ru") log.messageRu else log.message
    val logColor = when {
        message.contains("BURST", ignoreCase = true) ||
                message.contains("BURST", ignoreCase = true) -> GoldAccent
        message.contains("eliminated", ignoreCase = true) ||
                message.contains("уничтожен", ignoreCase = true) -> HealthColor
        message.contains("fallen", ignoreCase = true) ||
                message.contains("пал", ignoreCase = true) -> HpBarLow
        message.contains("Level Up", ignoreCase = true) ||
                message.contains("уровня", ignoreCase = true) -> OrangeAccent
        message.contains("dropped", ignoreCase = true) ||
                message.contains("выпал", ignoreCase = true) -> EpicColor
        message.contains("revived", ignoreCase = true) ||
                message.contains("воскрешён", ignoreCase = true) -> HealthColor
        message.contains("CRIT", ignoreCase = true) -> GoldAccent
        else -> LogText
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LogBackground, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatLogTime(log.timestamp),
            fontSize = 11.sp,
            color = LogText.copy(alpha = 0.5f),
            modifier = Modifier.width(44.dp)
        )
        Text(
            text = message,
            fontSize = 13.sp,
            color = logColor,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatLogTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}