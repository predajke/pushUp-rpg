package com.pushupRPG.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushupRPG.app.ui.GameViewModel
import com.pushupRPG.app.ui.theme.*
import com.pushupRPG.app.utils.AchievementSystem
import com.pushupRPG.app.utils.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: GameViewModel,
    onNavigateToAchievements: () -> Unit,
    onNavigateToBestiary: () -> Unit,
    onNavigateToItemLog: () -> Unit,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val state = gameState ?: return
    val language = state.language

    val unlockedCount = viewModel.getUnlockedAchievements(state).size
    val totalCount = AchievementSystem.ALL.size
    val bestiaryCount = viewModel.getBestiary(state).size
    val itemLogCount = viewModel.getItemLog(state).size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = AppStrings.t(language, "progress"),
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProgressNavCard(
                title = AppStrings.t(language, "achievements"),
                subtitle = "$unlockedCount / $totalCount ${AppStrings.t(language, "unlocked")}",
                accentColor = OrangeAccent,
                backgroundRes = "bg_ach",
                onClick = onNavigateToAchievements
            )

            ProgressNavCard(
                title = AppStrings.t(language, "bestiary"),
                subtitle = "$bestiaryCount ${AppStrings.t(language, "encountered")}",
                accentColor = RareColor,
                backgroundRes = "bg_bestiary",
                onClick = onNavigateToBestiary
            )

            ProgressNavCard(
                title = AppStrings.t(language, "item_log"),
                subtitle = "${AppStrings.t(language, "last_items")} $itemLogCount ${AppStrings.t(language, "items_word")}",
                accentColor = EpicColor,
                backgroundRes = "bg_item_log",
                onClick = onNavigateToItemLog
            )
        }
    }
}

@Composable
private fun ProgressNavCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    backgroundRes: String = "",
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val bgResId = remember(backgroundRes) {
        if (backgroundRes.isNotEmpty())
            context.resources.getIdentifier(backgroundRes, "drawable", context.packageName)
        else 0
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        if (bgResId != 0) {
            Image(
                painter = painterResource(id = bgResId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.35f
            )
            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.35f)))
        } else {
            Box(modifier = Modifier.matchParentSize().background(DarkCard))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)
                    .background(accentColor)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}
