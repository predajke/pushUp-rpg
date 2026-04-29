package com.ninthbalcony.pushuprpg.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import com.ninthbalcony.pushuprpg.ui.GameViewModel
import com.ninthbalcony.pushuprpg.ui.theme.*
import com.ninthbalcony.pushuprpg.utils.AchBonusType
import com.ninthbalcony.pushuprpg.utils.AchievementDef
import com.ninthbalcony.pushuprpg.utils.AchievementSystem
import com.ninthbalcony.pushuprpg.utils.AppStrings
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.ninthbalcony.pushuprpg.ui.preview.FakeGameRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val state = gameState ?: return
    val language = state.language ?: "en"

    val unlocked = viewModel.getUnlockedAchievements(state)
    val unlockedIds = unlocked.map { it.defId }.toSet()

    var activeIds by remember(state.activeAchievementIds) {
        mutableStateOf(
            state.activeAchievementIds.split(",").filter { it.isNotEmpty() }.toMutableList()
        )
    }

    val uniqueDefs = AchievementSystem.ALL.filter { it.tier == 0 }
    val progressiveDefs = AchievementSystem.ALL.filter { it.tier > 0 }

    val context = LocalContext.current
    val achBtnBgId = remember {
        context.resources.getIdentifier("bg_actach_panel", "drawable", context.packageName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = AppStrings.t(language, "achievements"),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${unlockedIds.size} / ${AchievementSystem.ALL.size}",
                color = TextSecondary,
                fontSize = 14.sp
            )
            if (activeIds.isNotEmpty()) {
                TextButton(onClick = { activeIds = mutableListOf() }) {
                    Text(
                        text = AppStrings.t(language, "btn_unselect_all"),
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Text(
                    text = AppStrings.t(language, "ach_unique"),
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(uniqueDefs) { def ->
                AchievementRow(
                    def = def,
                    language = language,
                    isUnlocked = def.id in unlockedIds,
                    isActive = def.id in activeIds,
                    progressText = if (def.id !in unlockedIds) AchievementSystem.getProgressText(def, state, language) else null,
                    onActiveChange = { checked ->
                        val current = activeIds.toMutableList()
                        if (checked && current.size < 3) {
                            current.add(def.id)
                        } else if (!checked) {
                            current.remove(def.id)
                        }
                        activeIds = current
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = AppStrings.t(language, "ach_progressive"),
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(progressiveDefs) { def ->
                AchievementRow(
                    def = def,
                    language = language,
                    isUnlocked = def.id in unlockedIds,
                    isActive = def.id in activeIds,
                    progressText = if (def.id !in unlockedIds) AchievementSystem.getProgressText(def, state, language) else null,
                    onActiveChange = { checked ->
                        val current = activeIds.toMutableList()
                        if (checked && current.size < 3) {
                            current.add(def.id)
                        } else if (!checked) {
                            current.remove(def.id)
                        }
                        activeIds = current
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 48.dp, vertical = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                .background(Color(0xFF1565C0))
                .clickable {
                    viewModel.setActiveAchievements(activeIds.toList())
                    onBack()
                },
            contentAlignment = Alignment.Center
        ) {
            if (achBtnBgId != 0) {
                Image(
                    painter = painterResource(id = achBtnBgId),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.3f
                )
            }
            Text(
                text = "${AppStrings.t(language, "btn_save")} (${activeIds.size}/3)",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun AchievementRow(
    def: AchievementDef,
    language: String,
    isUnlocked: Boolean,
    isActive: Boolean,
    progressText: String? = null,
    onActiveChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val bgColor = if (isActive) DarkSurfaceVariant else DarkCard
    val textColor = if (isUnlocked) TextPrimary else TextMuted
    val subtextColor = if (isUnlocked) TextSecondary else TextMuted
    val dotColor = achBonusDotColor(def.bonusType)
    val name = if (language == "ru") def.nameRu else def.nameEn
    val bonusLabel = AchievementSystem.getBonusLabel(def, language)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val resId = context.resources.getIdentifier(def.imageRes, "drawable", context.packageName)
        if (resId != 0) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = def.id.first().uppercaseChar().toString(),
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Row(
            modifier = Modifier.size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isUnlocked) dotColor else TextMuted)
        ) {}

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, color = textColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(text = bonusLabel, color = subtextColor, fontSize = 12.sp)
            if (progressText != null) {
                Text(text = progressText, color = TextMuted, fontSize = 11.sp)
            }
        }

        if (isUnlocked) {
            Checkbox(
                checked = isActive,
                onCheckedChange = onActiveChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = OrangeAccent,
                    uncheckedColor = TextMuted
                )
            )
        }
    }
}

private fun achBonusDotColor(type: AchBonusType): Color = when (type) {
    AchBonusType.DAMAGE_PERCENT       -> Color(0xFFFF4444)
    AchBonusType.XP_PERCENT           -> Color(0xFFFFD700)
    AchBonusType.DROP_RATE_PERCENT    -> Color(0xFF4CAF50)
    AchBonusType.ARMOR_PERCENT        -> Color(0xFF2196F3)
    AchBonusType.HP_FLAT              -> Color(0xFF44CC44)
    AchBonusType.CRIT_PERCENT         -> Color(0xFFFF6B00)
    AchBonusType.ENCHANT_FLAT         -> Color(0xFF9C27B0)
    AchBonusType.TEETH_RATE_PERCENT   -> Color(0xFFFFEE58)
}

@Preview(showBackground = true, widthDp = 412, heightDp = 920)
@Composable
private fun AchievementsScreenPreview() {
    val vm = remember { GameViewModel(FakeGameRepository()) }
    AchievementsScreen(viewModel = vm, onBack = {})
}
