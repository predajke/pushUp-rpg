package com.ninthbalcony.pushuprpg.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BestiaryScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val state = gameState ?: return
    val language = state.language ?: "en"
    val bestiary = viewModel.getBestiary(state)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = AppStrings.t(language, "bestiary"),
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

            item {
                Spacer(modifier = Modifier.height(8.dp))
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

@Composable
private fun BestiaryCard(
    monster: Monster,
    language: String,
    kills: Int
) {
    val context = LocalContext.current
    val encountered = kills > 0
    val displayName = if (language == "ru") monster.nameRu else monster.name

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkCard)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val resId = context.resources.getIdentifier(monster.imageRes, "drawable", context.packageName)
        Box(
            modifier = Modifier
                .size(48.dp)
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
}
