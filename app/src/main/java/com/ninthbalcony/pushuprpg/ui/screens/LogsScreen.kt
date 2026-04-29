package com.ninthbalcony.pushuprpg.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninthbalcony.pushuprpg.data.db.LogEntryEntity
import com.ninthbalcony.pushuprpg.ui.theme.*
// ИСПРАВЛЕНО 1: Обновлен путь к GameViewModel
import com.ninthbalcony.pushuprpg.ui.GameViewModel
import com.ninthbalcony.pushuprpg.utils.AppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.ninthbalcony.pushuprpg.ui.preview.FakeGameRepository

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
                text = AppStrings.t(language, "battle_log"),
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
                    text = AppStrings.t(language, "logs_empty"),
                    fontSize = 16.sp,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .navigationBarsPadding(),
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
        message.contains("★LEGENDARY★") -> GoldAccent
        message.startsWith("💪") -> HealthColor
        message.contains("Auto-attack", ignoreCase = true) ||
                message.contains("Авто-атака", ignoreCase = true) -> HealthColor
        message.contains("strikes back", ignoreCase = true) ||
                message.contains("бьёт в ответ", ignoreCase = true) ||
                message.startsWith("⚔️") ||
                message.startsWith("🪓") -> HpBarLow
        message.contains("BURST", ignoreCase = true) -> GoldAccent
        message.contains("CRIT", ignoreCase = true) -> GoldAccent
        message.contains("Level Up", ignoreCase = true) ||
                message.contains("уровня", ignoreCase = true) -> OrangeAccent
        message.contains("dropped", ignoreCase = true) ||
                message.contains("выпал", ignoreCase = true) ||
                message.contains("дроп", ignoreCase = true) -> EpicColor
        message.contains("revived", ignoreCase = true) ||
                message.contains("воскрешён", ignoreCase = true) -> HealthColor
        message.startsWith("Forge:") ||
                message.startsWith("Кузница:") -> OrangeAccent
        message.startsWith("⚡") -> Color(0xFFFF80AB)
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

@Preview(showBackground = true, widthDp = 412, heightDp = 920)
@Composable
private fun LogsScreenPreview() {
    val vm = remember { GameViewModel(FakeGameRepository()) }
    LogsScreen(viewModel = vm, onBack = {})
}
