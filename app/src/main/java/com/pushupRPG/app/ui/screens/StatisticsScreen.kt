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
import com.pushupRPG.app.data.model.PeriodStats
import com.pushupRPG.app.ui.theme.*
import com.pushupRPG.app.ui.GameViewModel
import com.pushupRPG.app.utils.AppStrings

@Composable
fun StatisticsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val periodStats by viewModel.periodStats.collectAsState(initial = null)
    val weekStats by viewModel.weekStats.collectAsState(initial = emptyList())

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
                text = AppStrings.t(language, "statistics"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
                .navigationBarsPadding(),
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
            text = AppStrings.t(language, "stats_last_week"),
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
                    text = AppStrings.t(language, "stats_no_data"),
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
    periodStats: PeriodStats?,
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
            label = AppStrings.t(language, "period_week"),
            value = "${periodStats?.lastWeek ?: 0}"
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "period_month"),
            value = "${periodStats?.lastMonth ?: 0}"
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "period_quarter"),
            value = "${periodStats?.lastQuarter ?: 0}"
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "period_year"),
            value = "${periodStats?.lastYear ?: 0}"
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "period_all"),
            value = "${periodStats?.total ?: 0}",
            valueColor = OrangeAccent
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "streak_current"),
            value = "${gameState?.currentStreak ?: 0} ${AppStrings.t(language, "streak_days")}"
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "streak_best"),
            value = "${gameState?.longestStreak ?: 0} ${AppStrings.t(language, "streak_days")}",
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
            text = AppStrings.t(language, "rpg_stats"),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = OrangeAccent,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        StatRow(
            label = AppStrings.t(language, "stat_level"),
            value = "${state.playerLevel}",
            valueColor = GoldAccent
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "items_collected"),
            value = "${state.itemsCollected}"
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "dmg_dealt"),
            value = "${state.totalDamageDealt}"
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "dmg_highest"),
            value = "${state.highestDamage}",
            valueColor = PowerColor
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "enemies_killed"),
            value = "${state.monstersKilled}"
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "char_born"),
            value = if (state.characterBirthDate.isNotEmpty())
                state.characterBirthDate
            else
                AppStrings.t(language, "unknown"),
            valueColor = TextSecondary
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "best_session"),
            value = if (state.bestSingleSession > 0) "${state.bestSingleSession}" else "—",
            valueColor = GoldAccent
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "crit_hits"),
            value = "${state.totalCriticalHits}"
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "enchants_done"),
            value = "${state.totalEnchantmentsSuccess}",
            valueColor = GoldAccent
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "items_merged"),
            value = "${state.totalItemsMerged}"
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "teeth_spent"),
            value = "${state.totalTeethSpent}"
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "teeth_earned"),
            value = "${state.totalTeethEarned}"
        )
        StatDivider()
        StatRow(
            label = AppStrings.t(language, "highest_monster"),
            value = if (state.highestMonsterLevelKilled > 0) "lvl ${state.highestMonsterLevelKilled}" else "—",
            valueColor = PowerColor
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