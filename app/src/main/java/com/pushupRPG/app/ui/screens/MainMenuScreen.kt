package com.pushupRPG.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pushupRPG.app.data.db.GameStateEntity
import com.pushupRPG.app.data.db.LogEntryEntity
import com.pushupRPG.app.ui.theme.*
import com.pushupRPG.app.utils.EventUtils
import com.pushupRPG.app.utils.GameCalculations
import com.pushupRPG.app.utils.MonsterUtils
import com.pushupRPG.app.ui.GameViewModel // ИСПРАВЛЕН ИМПОРТ

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    onNavigateToInventory: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToShop: () -> Unit
) {
    // ИСПРАВЛЕНО: Добавлены начальные значения (initial) и заменен logs на recentLogs
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val logs by viewModel.recentLogs.collectAsState(initial = emptyList())
    val inputValue by viewModel.inputValue.collectAsState(initial = 0)
    val showLevelUpDialog by viewModel.showLevelUpDialog.collectAsState(initial = false)
    val newLevel by viewModel.newLevel.collectAsState(initial = 0)
    val totalStats by viewModel.totalStats.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val activeEvent by viewModel.activeEvent.collectAsState(initial = null)

    if (isLoading || gameState == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OrangeAccent)
        }
        return
    }

    val state = gameState!!

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000L)
            viewModel.triggerRealtimeTick()
        }
    }

    val language = state.language
    val maxHp = GameCalculations.getMaxHp(
        state.playerLevel,
        state.baseHealth,
        viewModel.getEquippedItems(state).sumOf { it.stats.health }
    )

    if (showLevelUpDialog) {
        LevelUpDialog(
            newLevel = newLevel,
            unspentPoints = state.unspentStatPoints,
            language = language,
            onSpendPoint = { stat -> viewModel.spendStatPoint(stat) },
            onDismiss = { viewModel.dismissLevelUpDialog() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        TopBar(
            state = state,
            maxHp = maxHp,
            onSettingsClick = onNavigateToSettings
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
                .navigationBarsPadding()
        ) {
            EventBanner(
                language = language,
                activeEvent = activeEvent,
                eventEndTime = state.eventEndTime
            )

            Spacer(modifier = Modifier.height(10.dp))

            PushUpCounter(
                state = state,
                inputValue = inputValue,
                language = language,
                onAddToInput = { viewModel.addToInput(it) },
                onReset = { viewModel.resetInput() },
                onSave = { viewModel.savePushUps() },
                onTotalClick = onNavigateToStatistics,
                onShopClick = onNavigateToShop
            )

            Spacer(modifier = Modifier.height(10.dp))

            StatsPanel(
                state = state,
                totalStats = totalStats,
                onClick = onNavigateToInventory
            )

            Spacer(modifier = Modifier.height(10.dp))

            BattleArena(
                state = state,
                maxHp = maxHp,
                modifier = Modifier.heightIn(min = 220.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            MiniLog(
                logs = logs,
                language = language,
                onClick = onNavigateToLogs
            )
        }
    }
}

// --- Шапка ---
@Composable
fun TopBar(
    state: GameStateEntity,
    maxHp: Int,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(OrangeAccent, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Lvl ${state.playerLevel}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            LinearProgressIndicator(
                progress = { GameCalculations.getXpProgress(state.totalXp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = OrangeAccent,
                trackColor = DarkSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🔥 Streak ${state.currentStreak} days",
                    fontSize = 11.sp,
                    color = OrangeLight
                )
                Text(
                    text = "${GameCalculations.getXpForNextLevel(state.totalXp)} xp to next",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onSettingsClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .border(2.dp, OrangeAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👤", fontSize = 20.sp)
            }
            Text(
                text = state.playerName,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// --- Баннер события ---
@Composable
fun EventBanner(
    language: String,
    activeEvent: com.pushupRPG.app.data.model.GameEvent?,
    eventEndTime: Long
) {
    val borderColor = if (activeEvent != null) GoldAccent else OrangeAccent.copy(alpha = 0.3f)
    val bgColor = if (activeEvent != null) Color(0xFF1A1500) else DarkSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        if (activeEvent == null) {
            Text(
                text = if (language == "ru")
                    "Событий пока нет. Продолжай тренироваться!"
                else
                    "No events right now. Keep training!",
                fontSize = 13.sp,
                color = TextSecondary
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = activeEvent.icon, fontSize = 20.sp)
                    Column {
                        Text(
                            text = EventUtils.getEventName(activeEvent, language),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Text(
                            text = EventUtils.getEventDescription(activeEvent, language),
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                Text(
                    text = EventUtils.getRemainingTime(eventEndTime),
                    fontSize = 11.sp,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- Счётчик отжиманий ---
@Composable
fun PushUpCounter(
    state: GameStateEntity,
    inputValue: Int,
    language: String,
    onAddToInput: (Int) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onTotalClick: () -> Unit,
    onShopClick: () -> Unit
) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_pushups", "drawable", context.packageName)
    }
    val shopBtnBg = remember {
        context.resources.getIdentifier("bg_shop_btn", "drawable", context.packageName)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(DarkCard, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (bgResId != 0) {
            Image(
                painter = painterResource(id = bgResId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.15f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (language == "ru") "Отжимания сегодня" else "Total Push Ups Today",
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier.fillMaxWidth().clickable { onTotalClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${state.pushUpsToday}",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent,
                    lineHeight = 48.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(82.dp)
                ) {
                    Button(
                        onClick = onReset,
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonGray),
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = if (language == "ru") "Сброс" else "Reset", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onAddToInput(-1) },
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonRed),
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "-1", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .width(68.dp)
                        .height(68.dp)
                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                        .border(1.dp, OrangeAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+$inputValue",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeAccent,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(82.dp)
                ) {
                    Button(
                        onClick = { onAddToInput(10) },
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "+10", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onAddToInput(1) },
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "+1", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .background(Color(0xFF1A3A2A), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onShopClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (shopBtnBg != 0) {
                        Image(
                            painter = painterResource(id = shopBtnBg),
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.3f
                        )
                    }
                    Text(
                        text = if (language == "ru") "Магазин" else "Shop",
                        fontSize = 12.sp,
                        color = HealthColor,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (language == "ru") "Сохранить" else "Save",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                        .border(1.dp, OrangeAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { onTotalClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "›", fontSize = 22.sp, color = OrangeAccent, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- Панель статов ---
@Composable
fun StatsPanel(
    state: GameStateEntity,
    totalStats: com.pushupRPG.app.utils.TotalStats?,
    onClick: () -> Unit
) {
    val power = totalStats?.power ?: state.basePower
    val armor = totalStats?.armor ?: state.baseArmor
    val health = totalStats?.health ?: state.baseHealth
    val luck = totalStats?.luck ?: state.baseLuck

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(DarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, OrangeAccent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(icon = "⚔️", value = "$power", color = PowerColor)
        StatItem(icon = "🛡️", value = "$armor", color = ArmorColor)
        StatItem(icon = "❤️", value = "$health", color = HealthColor)
        StatItem(icon = "🍀", value = String.format("%.1f", luck), color = LuckColor)
        StatItem(icon = "🦷", value = "${state.teeth}", color = Color(0xFFE0E0E0))

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "›",
            fontSize = 24.sp,
            color = OrangeAccent.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatItem(icon: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, fontSize = 16.sp)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// --- Арена ---
@Composable
fun BattleArena(
    state: GameStateEntity,
    maxHp: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bgResId = remember {
        val index = (1..5).random()
        context.resources.getIdentifier("bg_fight_$index", "drawable", context.packageName)
    }

    val heroImageRes = state.heroAvatar.ifEmpty { MonsterUtils.getHeroImageRes(state.playerLevel) }
    val monsterImageRes = "monster_${state.monsterLevel}"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .then(modifier)
            .background(DarkCard, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (bgResId != 0) {
            Image(
                painter = painterResource(id = bgResId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.2f
            )
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val hpPercent = if (maxHp > 0) state.currentHp.toFloat() / maxHp else 0f
                    val hpColor = when {
                        hpPercent > 0.6f -> HpBarFull
                        hpPercent > 0.3f -> HpBarMid
                        else -> HpBarLow
                    }
                    Text(text = "${state.currentHp}/${maxHp} HP", fontSize = 11.sp, color = hpColor)
                    LinearProgressIndicator(
                        progress = { hpPercent.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = hpColor,
                        trackColor = HpBarBackground
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (state.isPlayerDead) "💀" else "⚔️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val monsterHpPercent = if (state.monsterMaxHp > 0)
                        state.monsterCurrentHp.toFloat() / state.monsterMaxHp else 0f
                    Text(
                        text = "${state.monsterCurrentHp}/${state.monsterMaxHp} HP",
                        fontSize = 11.sp,
                        color = HpBarLow,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LinearProgressIndicator(
                        progress = { monsterHpPercent.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = HpBarLow,
                        trackColor = HpBarBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    DrawableImage(
                        name = heroImageRes,
                        modifier = Modifier
                            .size(110.dp)
                            .alpha(if (state.isPlayerDead) 0.4f else 1f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.playerName,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(
                    text = "VS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    DrawableImage(
                        name = monsterImageRes,
                        modifier = Modifier.size(110.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val monster = MonsterUtils.getMonsterByLevel(state.monsterLevel)
                    val monsterName = MonsterUtils.getMonsterName(monster, state.language)
                    Text(
                        text = "$monsterName Lv${state.monsterLevel}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// --- Мини-лог ---
@Composable
fun MiniLog(
    logs: List<LogEntryEntity>,
    language: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(LogBackground, RoundedCornerShape(12.dp))
            .border(1.dp, LogText.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        val recentLogs = logs.take(4)

        if (recentLogs.isEmpty()) {
            Text(
                text = if (language == "ru") "Бой начнётся скоро..." else "Battle will begin soon...",
                fontSize = 13.sp,
                color = LogText.copy(alpha = 0.6f)
            )
        } else {
            recentLogs.forEach { log ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Text(
                        text = formatTimestamp(log.timestamp),
                        fontSize = 12.sp,
                        color = LogText.copy(alpha = 0.6f),
                        modifier = Modifier.width(44.dp)
                    )
                    Text(
                        text = if (language == "ru") log.messageRu else log.message,
                        fontSize = 12.sp,
                        color = LogText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (language == "ru") "→ все логи" else "→ view all logs",
            fontSize = 11.sp,
            color = LogText.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.End)
        )
    }
}

// --- Level Up диалог ---
@Composable
fun LevelUpDialog(
    newLevel: Int,
    unspentPoints: Int,
    language: String,
    onSpendPoint: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_levelup", "drawable", context.packageName)
    }
    val iconResId = remember {
        context.resources.getIdentifier("lvlup", "drawable", context.packageName)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (bgResId != 0) {
                Image(
                    painter = painterResource(id = bgResId),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.25f
                )
            }

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (iconResId != 0) {
                    Image(
                        painter = painterResource(id = iconResId),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(text = "⬆️", fontSize = 48.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (language == "ru") "Повышение уровня!" else "Level Up!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                Text(
                    text = if (language == "ru") "Уровень $newLevel" else "Level $newLevel",
                    fontSize = 16.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (language == "ru")
                        "Очков для распределения: $unspentPoints"
                    else
                        "Points to spend: $unspentPoints",
                    fontSize = 14.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatPointButton(
                        icon = "⚔️",
                        label = if (language == "ru") "Сила" else "Power",
                        enabled = unspentPoints > 0,
                        onClick = { onSpendPoint("power") }
                    )
                    StatPointButton(
                        icon = "❤️",
                        label = if (language == "ru") "HP" else "Health",
                        enabled = unspentPoints > 0,
                        onClick = { onSpendPoint("health") }
                    )
                    StatPointButton(
                        icon = "🍀",
                        label = if (language == "ru") "Удача" else "Luck",
                        enabled = unspentPoints > 0,
                        onClick = { onSpendPoint("luck") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (unspentPoints <= 0) OrangeAccent else GoldAccent
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (unspentPoints <= 0) {
                            if (language == "ru") "Продолжить" else "Continue"
                        } else {
                            if (language == "ru") "Позже" else "Later"
                        },
                        fontWeight = FontWeight.Bold,
                        color = if (unspentPoints <= 0) Color.White else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun StatPointButton(
    icon: String,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkSurfaceVariant,
            disabledContainerColor = DarkSurfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(72.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 24.sp)
            Text(
                text = label,
                fontSize = 10.sp,
                color = if (enabled) TextPrimary else TextMuted
            )
        }
    }
}

@Composable
fun DrawableImage(
    name: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resId = remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }

    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = name,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = modifier.background(DarkSurfaceVariant, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "?", fontSize = 32.sp, color = TextMuted)
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}