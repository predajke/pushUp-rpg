package com.ninthbalcony.pushuprpg.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
                QuestCard(quest = quest, language = language, index = index) {
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
                QuestCard(quest = quest, language = language) {
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

    val borderColor = when {
        isClaimed -> TextMuted
        isCompleted -> HealthColor
        def.isWeekly -> GoldAccent
        else -> OrangeAccent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
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
