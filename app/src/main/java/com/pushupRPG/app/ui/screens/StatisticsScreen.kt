package com.pushupRPG.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushupRPG.app.data.db.GameStateEntity
import com.pushupRPG.app.ui.theme.*
import com.pushupRPG.app.viewmodel.GameViewModel

@Composable
fun StatisticsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val periodStats by viewModel.periodStats.collectAsState()
    val weekStats by viewModel.weekStats.collectAsState()

    val language = gameState?.language ?: "en"

    LaunchedEffect(Unit) {
        viewModel.loadPeriodStats()
    }

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
                text = if (language == "ru") "Статистика" else "Statistics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- График за неделю ---
            WeeklyChart(
                weekStats = weekStats,
                language = language
            )

            // --- Статистика отжиманий ---
            PushUpStatsCard(
                periodStats = periodStats,
                gameState = gameState,
                language = language
            )

            // --- RPG статистика ---
            gameState?.let { state ->
                RpgStatsCard(
                    state = state,
                    language = language
                )
            }
        }
    }
}

// --- График за неделю ---
@Composable
fun WeeklyChart(
    weekStats: List<Pair<String, Int>>,
    language: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = if (language == "ru") "Последняя неделя" else "Last week",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (weekStats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (language == "ru")
                        "Нет данных за эту неделю"
                    else
                        "No data for this week",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            val maxValue = weekStats.maxOfOrNull { it.second } ?: 1

            // Значения над столбиками
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekStats.forEach { (_, count) ->
                    Text(
                        text = "$count",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Столбики
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weekStats.forEach { (_, count) ->
                    val heightPercent = if (maxValue > 0) {
                        count.toFloat() / maxValue.toFloat()
                    } else 0f

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(heightPercent.coerceAtLeast(0.02f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (count > 0) OrangeAccent
                                    else DarkSurfaceVariant
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Дни недели
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekStats.forEach { (day, _) ->
                    Text(
                        text = day,
                        fontSize = 11.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// --- Статистика отжиманий ---
@Composable
fun PushUpStatsCard(
    periodStats: com.pushupRPG.app.data.repository.PeriodStats?,
    gameState: GameStateEntity?,
    language: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        StatRow(
            label = if (language == "ru") "За последнюю неделю" else "Last week",
            value = "${periodStats?.lastWeek ?: 0}"
        )
        StatDivider()
        StatRow(
            label = if (language == "ru") "За последний месяц" else "Last month",
            value = "${periodStats?.lastMonth ?: 0}"
        )
        StatDivider()
        StatRow(
            label = if (language == "ru") "За последний квартал" else "Last quarter",
            value = "${periodStats?.lastQuarter ?: 0}"
        )
        StatDivider()
        StatRow(
            label = if (language == "ru") "За последний год" else "Last year",
            value = "${periodStats?.lastYear ?: 0}"
        )
        StatDivider()
        StatRow(
            label = if (language == "ru") "За всё время" else "Total",
            value = "${periodStats?.total ?: 0}",
            valueColor = OrangeAccent
        )
        StatDivider()
        StatRow(
            label = if (language == "ru") "Текущий стрик" else "Current Streak",
            value = "${gameState?.currentStreak ?: 0} ${if (language == "ru") "дн." else "days"}"
        )
        StatDivider()
        StatRow(
            label = if (language == "ru") "Лучший стрик" else "Longest Streak",
            value = "${gameState?.longestStreak ?: 0} ${if (language == "ru") "дн." else "days"}",
            valueColor = GoldAccent
        )
    }
}

// --- RPG статистика ---
@Composable
fun RpgStatsCard(
    state: GameStateEntity,
    language: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = if (language == "ru") "RPG Статистика" else "RPG Stats",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = OrangeAccent,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        StatRow(
            label = if (language == "ru") "Уровень" else "Level",
            value = "${state.playerLevel}",
            valueColor = GoldAccent
        )
        StatDivider()
        StatRow(
            label = if (language == "ru") "Предметов получено" else "Items collected",
            value = "${state.itemsCollected}"
        )
        StatDivider()
        StatRow(
            label = if (language == "ru") "Урона нанесено" else "DMG dealt",
            value = "${state.totalDamageDealt}"
        )
        StatDivider()
        StatRow(
            label = if (language == "ru") "Макс. удар" else "Highest DMG",
            value = "${state.highestDamage}",
            valueColor = PowerColor
        )
        StatDivider()
        StatRow(
            label = if (language == "ru") "Врагов убито" else "Enemies killed",
            value = "${state.monstersKilled}"
        )
        StatDivider()
        StatRow(
            label = if (language == "ru") "Дата рождения" else "Character born",
            value = if (state.characterBirthDate.isNotEmpty())
                state.characterBirthDate
            else
                if (language == "ru") "Неизвестно" else "Unknown",
            valueColor = TextSecondary
        )
    }
}

// --- Вспомогательные ---
@Composable
fun StatRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun StatDivider() {
    HorizontalDivider(
        color = DarkSurfaceVariant,
        thickness = 1.dp
    )
}