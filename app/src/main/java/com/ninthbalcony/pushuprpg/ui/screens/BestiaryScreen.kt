package com.ninthbalcony.pushuprpg.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninthbalcony.pushuprpg.data.model.Monster
import com.ninthbalcony.pushuprpg.ui.GameViewModel
import com.ninthbalcony.pushuprpg.ui.theme.*
import com.ninthbalcony.pushuprpg.utils.AppStrings
import com.ninthbalcony.pushuprpg.utils.BossUtils
import com.ninthbalcony.pushuprpg.utils.MonsterUtils
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.ninthbalcony.pushuprpg.ui.preview.FakeGameRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BestiaryScreen(
    viewModel: GameViewModel,
    bossesOnly: Boolean = false,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val state = gameState ?: return
    val language = state.language ?: "en"
    val bestiary = viewModel.getBestiary(state)

    val title = if (bossesOnly)
        AppStrings.t(language, "bosses")
    else
        AppStrings.t(language, "bestiary")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = title,
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

        LazyColumn(
            modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!bossesOnly) {
                item {
                    Text(
                        text = AppStrings.t(language, "monsters"),
                        color = OrangeAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(MonsterUtils.getAllMonsters()) { monster ->
                    BestiaryCard(
                        monster = monster,
                        language = language,
                        kills = bestiary[monster.name] ?: 0
                    )
                }
            } else {
                item {
                    Text(
                        text = AppStrings.t(language, "bosses"),
                        color = OrangeAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(BossUtils.getAllBosses()) { boss ->
                    BestiaryCard(
                        monster = boss,
                        language = language,
                        kills = bestiary[boss.name] ?: 0
                    )
                }
            }
        }
    }
}

@Composable
private fun BestiaryCard(
    monster: Monster,
    language: String,
    kills: Int
) {
    val context = LocalContext.current
    val encountered = kills > 0
    val displayName = if (language == "ru") monster.nameRu else monster.name
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkCard)
            .clickable(enabled = encountered) { expanded = !expanded }
            .padding(12.dp)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imgSize = if (expanded) 124.dp else 48.dp
            val resId = context.resources.getIdentifier(monster.imageRes, "drawable", context.packageName)
            Box(
                modifier = Modifier
                    .size(imgSize)
                    .alpha(if (encountered) 1f else 0.5f)
            ) {
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = displayName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayName.first().uppercaseChar().toString(),
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    color = if (encountered) TextPrimary else TextMuted,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = "${AppStrings.t(language, "lv_prefix")} ${monster.level}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            if (encountered) {
                Text(
                    text = "$kills ${AppStrings.t(language, "kills")}",
                    color = OrangeAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = AppStrings.t(language, "not_encountered"),
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }

        if (expanded && encountered) {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DarkSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MonsterStatChip(
                    label = if (language == "ru") "HP" else "HP",
                    value = monster.maxHp.toString()
                )
                MonsterStatChip(
                    label = if (language == "ru") "Урон" else "Damage",
                    value = monster.damage.toString()
                )
                MonsterStatChip(
                    label = if (language == "ru") "Дроп" else "Drop",
                    value = "${(monster.dropRate * 100).toInt()}%"
                )
            }
        }
    }
}

@Composable
private fun MonsterStatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextMuted, fontSize = 11.sp)
        Text(text = value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 920)
@Composable
private fun BestiaryScreenPreview() {
    val vm = remember { GameViewModel(FakeGameRepository()) }
    BestiaryScreen(viewModel = vm, onBack = {})
}
