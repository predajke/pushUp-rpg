package com.ninthbalcony.pushuprpg.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninthbalcony.pushuprpg.ui.GameViewModel
import com.ninthbalcony.pushuprpg.ui.theme.*
import com.ninthbalcony.pushuprpg.utils.ActiveQuest
import com.ninthbalcony.pushuprpg.utils.AppStrings
import com.ninthbalcony.pushuprpg.utils.QuestSystem
import com.ninthbalcony.pushuprpg.utils.SoundManager
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.ninthbalcony.pushuprpg.ui.preview.FakeGameRepository

@Composable
fun QuestsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val state = gameState ?: return

    val language = state.language
    val quests = viewModel.getActiveQuests(state)
    val daily = quests.filter { QuestSystem.getDefById(it.defId)?.isWeekly == false }
    val weekly = quests.filter { QuestSystem.getDefById(it.defId)?.isWeekly == true }

    val adQuestRerollPending by viewModel.adQuestRerollPending.collectAsState()
    val activity = LocalActivity.current

    val context = LocalContext.current
    val soundEnabled = remember {
        context.getSharedPreferences("pushup_prefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("sounds_enabled", true)
    }
    val vibrationEnabled = remember {
        context.getSharedPreferences("pushup_prefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("vibration_enabled", true)
    }

    if (adQuestRerollPending) {
        com.ninthbalcony.pushuprpg.ui.dialogs.RewardedAdDialog(
            title = AppStrings.t(language, "ad_title"),
            description = if (language == "ru") "Получите 3 новых случайных ежедневных задания!" else "Get 3 new random daily quests!",
            rewardText = if (language == "ru") "3 новых задания" else "3 new quests",
            onWatchAd = { activity?.let { viewModel.playAdQuestReroll(it) } },
            onDecline = { viewModel.dismissAdQuestReroll() },
            onDismiss = { viewModel.dismissAdQuestReroll() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("‹", color = OrangeAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "📋 ${AppStrings.t(language, "quests")}",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    text = AppStrings.t(language, "quest_daily"),
                    color = OrangeAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            itemsIndexed(daily) { index, quest ->
                QuestCard(quest = quest, language = language, index = index, soundEnabled = soundEnabled, vibrationEnabled = vibrationEnabled, context = context) {
                    SoundManager.playSave(soundEnabled)
                    if (vibrationEnabled) vibrate(context)
                    viewModel.claimQuestReward(quest.defId)
                }
            }

            if (daily.isEmpty()) {
                item {
                    Text(
                        text = AppStrings.t(language, "quest_no_daily"),
                        color = TextMuted, fontSize = 13.sp
                    )
                }
            }

            item {
                val today = com.ninthbalcony.pushuprpg.utils.DateUtils.getTodayString()
                val alreadyRerolledToday = state.lastAdQuestRerollDate == today
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = { if (!alreadyRerolledToday) viewModel.requestAdQuestReroll() },
                        enabled = !alreadyRerolledToday,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (alreadyRerolledToday) TextMuted else OrangeAccent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (alreadyRerolledToday) TextMuted else OrangeAccent)
                    ) {
                        Text(
                            text = if (alreadyRerolledToday)
                                (if (language == "ru") "🎬 Перебросить задания (исп.)" else "🎬 Reroll Quests (used)")
                            else
                                (if (language == "ru") "🎬 Перебросить ежедневные задания" else "🎬 Watch Ad to Reroll Daily"),
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = AppStrings.t(language, "quest_weekly"),
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            items(weekly) { quest ->
                QuestCard(quest = quest, language = language, soundEnabled = soundEnabled, vibrationEnabled = vibrationEnabled, context = context) {
                    SoundManager.playSave(soundEnabled)
                    if (vibrationEnabled) vibrate(context)
                    viewModel.claimQuestReward(quest.defId)
                }
            }

            if (weekly.isEmpty()) {
                item {
                    Text(
                        text = AppStrings.t(language, "quest_no_weekly"),
                        color = TextMuted, fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestCard(
    quest: ActiveQuest,
    language: String,
    index: Int = 0,
    soundEnabled: Boolean = true,
    vibrationEnabled: Boolean = false,
    context: android.content.Context? = null,
    onClaim: () -> Unit
) {
    val context = LocalContext.current
    val def = QuestSystem.getDefById(quest.defId) ?: return
    val progress = quest.progress
    val target = def.target
    val fraction = (progress.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val isCompleted = quest.isCompleted
    val isClaimed = quest.claimed

    val bgImageName = when {
        def.isWeekly -> "bg_quest_weekly"
        index % 2 == 0 -> "bg_quest_daily"
        else -> "bg_quest_daily_2"
    }
    val bgResId = remember(bgImageName) {
        context.resources.getIdentifier(bgImageName, "drawable", context.packageName)
    }

    val baseBorderColor = when {
        isClaimed -> TextMuted
        isCompleted -> HealthColor
        def.isWeekly -> GoldAccent
        else -> OrangeAccent
    }

    val infiniteTransition = rememberInfiniteTransition(label = "questBorder")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isCompleted && !isClaimed) 0.4f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "questAlpha"
    )
    val borderColor = if (isCompleted && !isClaimed) baseBorderColor.copy(alpha = pulseAlpha) else baseBorderColor

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Слой 1: фон
        if (bgResId != 0) {
            Image(
                painter = painterResource(id = bgResId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        }
        val overlayAlpha = when {
            isClaimed -> 0.80f
            bgResId != 0 -> 0.55f
            else -> 1f
        }
        Box(modifier = Modifier.matchParentSize()
            .background(Color.Black.copy(alpha = overlayAlpha)))

        // Слой 2: контент
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = QuestSystem.getName(def, language),
                    color = if (isClaimed) TextMuted else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = QuestSystem.getDesc(def, language),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = QuestSystem.getRewardText(def, language),
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(DarkSurfaceVariant, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .background(if (isCompleted) HealthColor else OrangeAccent, RoundedCornerShape(3.dp))
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$progress / $target",
                color = TextSecondary,
                fontSize = 12.sp
            )
            when {
                isClaimed -> Text(
                    text = AppStrings.t(language, "quest_claimed"),
                    color = TextMuted, fontSize = 12.sp
                )
                isCompleted -> Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = HealthColor),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = AppStrings.t(language, "btn_claim"),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                else -> {}
            }
        }
        } // Column (контент)
    } // Box (карточка)
}

private fun vibrate(context: android.content.Context) {
    val vib = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val vm = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE)
                as android.os.VibratorManager
        vm.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
    }
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        vib.vibrate(android.os.VibrationEffect.createOneShot(80, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vib.vibrate(80)
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 920)
@Composable
private fun QuestsScreenPreview() {
    val vm = remember { GameViewModel(FakeGameRepository()) }
    QuestsScreen(viewModel = vm, onBack = {})
}
