package com.ninthbalcony.pushuprpg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import com.ninthbalcony.pushuprpg.data.model.PeriodStats
import com.ninthbalcony.pushuprpg.ui.theme.*
import com.ninthbalcony.pushuprpg.ui.GameViewModel
import com.ninthbalcony.pushuprpg.utils.AppStrings
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.ninthbalcony.pushuprpg.ui.preview.FakeGameRepository

@Composable
fun StatisticsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val periodStats by viewModel.periodStats.collectAsState(initial = null)
    val weekStats by viewModel.weekStats.collectAsState(initial = emptyList())
    val yearStats by viewModel.yearStats.collectAsState(initial = emptyList())

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
            // --- Сводные карточки ---
            val weekTotal = weekStats.sumOf { it.second }
            val yearTotal = yearStats.sumOf { it.second }
            val weekAvg = if (weekStats.isNotEmpty()) weekTotal / weekStats.size else 0
            SummaryStrip(weekTotal = weekTotal, yearTotal = yearTotal, weekAvg = weekAvg, language = language)

            // --- График за неделю ---
            val weekBest = weekStats.maxOfOrNull { it.second } ?: 0
            BarChartPanel(
                title = AppStrings.t(language, "stats_last_week"),
                subtitle = "$weekTotal ${if (language == "ru") "всего" else "total"} · ${AppStrings.t(language, "stats_best_day")} $weekBest",
                labels = weekStats.map { it.first },
                values = weekStats.map { it.second },
                showLegend = true,
                compact = false
            )

            // --- График за год ---
            val yearBest = yearStats.maxOfOrNull { it.second } ?: 0
            val yearBestLabel = yearStats.maxByOrNull { it.second }?.first ?: ""
            BarChartPanel(
                title = AppStrings.t(language, "stats_last_year"),
                subtitle = "$yearTotal ${if (language == "ru") "всего" else "total"} · ${AppStrings.t(language, "stats_best_month")} $yearBestLabel",
                labels = yearStats.map { it.first },
                values = yearStats.map { it.second },
                showLegend = false,
                compact = true
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

// --- Сводные карточки ---
@Composable
private fun SummaryStrip(weekTotal: Int, yearTotal: Int, weekAvg: Int, language: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryStatCard(
            label = AppStrings.t(language, "stats_this_week"),
            value = formatStatNum(weekTotal),
            modifier = Modifier.weight(1f)
        )
        SummaryStatCard(
            label = AppStrings.t(language, "stats_this_year"),
            value = formatStatNum(yearTotal),
            modifier = Modifier.weight(1f)
        )
        SummaryStatCard(
            label = AppStrings.t(language, "stats_daily_avg"),
            value = formatStatNum(weekAvg),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(DarkCard, RoundedCornerShape(18.dp))
            .border(0.5.dp, DarkSurfaceVariant, RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
            color = TextMuted
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = (-0.5).sp
        )
    }
}

// --- Столбчатый график (неделя / год) ---
@Composable
fun BarChartPanel(
    title: String,
    subtitle: String,
    labels: List<String>,
    values: List<Int>,
    showLegend: Boolean,
    compact: Boolean
) {
    var selected by remember { mutableStateOf<Int?>(null) }
    val tierColors = barTierColors(values)
    val maxVal = values.maxOrNull()?.takeIf { it > 0 } ?: 1
    val labelFontSize = if (compact) 9.sp else 11.sp
    val valueFontSize = if (compact) 9.sp else 11.sp
    val hPad = if (compact) 2.dp else 3.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
                }
            }
            if (showLegend) ChartLegend()
        }

        // Числа над столбиками
        Row(modifier = Modifier.fillMaxWidth()) {
            values.forEachIndexed { i, v ->
                Text(
                    text = if (v > 0) formatStatNum(v) else "",
                    fontSize = valueFontSize,
                    color = tierColors[i] ?: TextMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(2.dp))

        // Область столбиков
        Row(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            values.forEachIndexed { i, v ->
                val heightFrac = if (v > 0) (v.toFloat() / maxVal).coerceAtLeast(0.06f) else 0f
                val barColor = tierColors[i]
                val isSelected = selected == i

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = hPad)
                        .fillMaxHeight()
                        .clickable { selected = if (isSelected) null else i },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (v > 0) Modifier.fillMaxHeight(heightFrac) else Modifier.height(5.dp)
                            )
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(barColor ?: DarkSurfaceVariant.copy(alpha = 0.4f))
                            .then(
                                if (isSelected && barColor != null)
                                    Modifier.border(1.5.dp, barColor, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                else Modifier
                            )
                    )
                }
            }
        }

        HorizontalDivider(color = DarkSurfaceVariant.copy(alpha = 0.5f), thickness = 1.dp)
        Spacer(Modifier.height(6.dp))

        // Подписи оси X
        Row(modifier = Modifier.fillMaxWidth()) {
            labels.forEachIndexed { i, label ->
                Text(
                    text = label,
                    fontSize = labelFontSize,
                    color = if (selected == i) TextPrimary else TextMuted,
                    fontWeight = if (selected == i) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ChartLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf(ChartHigh to "Best", ChartMid to "Avg", ChartLow to "Low").forEach { (color, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(7.dp).background(color, RoundedCornerShape(2.dp)))
                Text(label, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private fun barTierColors(values: List<Int>): List<Color?> {
    val nonZero = values.filter { it > 0 }
    if (nonZero.isEmpty()) return values.map { null }
    val max = nonZero.maxOrNull()!!
    val min = nonZero.minOrNull()!!
    if (max == min) return values.map { if (it > 0) ChartHigh else null }
    val span = (max - min).toFloat()
    return values.map { v ->
        when {
            v == 0 -> null
            v >= min + span * 2f / 3f -> ChartHigh
            v <= min + span / 3f -> ChartLow
            else -> ChartMid
        }
    }
}

private fun formatStatNum(n: Int): String =
    if (n >= 1000) "${n / 1000},${"%03d".format(n % 1000)}" else "$n"

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

        // ── Вращение ленты ──
        StatSectionHeader(if (language == "ru") "🎰 Вращение ленты" else "🎰 Daily Spin")
        StatDivider()
        StatRow(
            label = if (language == "ru") "Зубы с ленты" else "Teeth from spin",
            value = "+${state.teethFromSpin} 🦷",
            valueColor = Color(0xFF4CAF50)
        )
        StatDivider()
        StatRow(
            label = if (language == "ru") "Вещи с ленты" else "Items from spin",
            value = "+${state.itemsFromSpin}",
            valueColor = Color(0xFF4CAF50)
        )

        // ── Источники зубов ──
        StatSectionHeader(if (language == "ru") "🦷 Источники зубов" else "🦷 Teeth sources")
        StatDivider()
        StatRow(
            label = if (language == "ru") "С квестов" else "From quests",
            value = "+${state.teethFromQuests} 🦷",
            valueColor = Color(0xFF4CAF50)
        )
        StatDivider()
        StatRow(
            label = if (language == "ru") "С рекламы" else "From ads",
            value = "+${state.teethFromAds} 🦷",
            valueColor = Color(0xFF4CAF50)
        )

        // ── Forge & Enchant ──
        StatSectionHeader(if (language == "ru") "🔨 Forge & Enchant" else "🔨 Forge & Enchant")
        StatDivider()
        StatRow(
            label = if (language == "ru") "Успешных merge" else "Successful merges",
            value = "${state.totalItemsMerged}",
            valueColor = GoldAccent
        )
        StatDivider()
        StatRow(
            label = if (language == "ru") "Неудачных merge" else "Failed merges",
            value = "${(state.totalMergeAttempts - state.totalItemsMerged).coerceAtLeast(0)}",
            valueColor = Color(0xFFE53935)
        )
        StatDivider()
        StatRow(
            label = if (language == "ru") "Успешных enchant" else "Successful enchants",
            value = "${state.totalEnchantmentsSuccess}",
            valueColor = GoldAccent
        )
        StatDivider()
        StatRow(
            label = if (language == "ru") "Неудачных enchant" else "Failed enchants",
            value = "${(state.totalEnchantAttempts - state.totalEnchantmentsSuccess).coerceAtLeast(0)}",
            valueColor = Color(0xFFE53935)
        )
    }
}

@Composable
private fun StatSectionHeader(text: String) {
    Text(
        text = text,
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
    )
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

@Preview(showBackground = true, widthDp = 412, heightDp = 920)
@Composable
private fun StatisticsScreenPreview() {
    val vm = remember { GameViewModel(FakeGameRepository()) }
    StatisticsScreen(viewModel = vm, onBack = {})
}
