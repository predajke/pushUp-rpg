package com.ninthbalcony.pushuprpg.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ninthbalcony.pushuprpg.ui.theme.ButtonGreen
import com.ninthbalcony.pushuprpg.ui.theme.DarkCard
import com.ninthbalcony.pushuprpg.ui.theme.TextPrimary
import com.ninthbalcony.pushuprpg.ui.theme.TextSecondary
import com.ninthbalcony.pushuprpg.ui.theme.UncommonColor
import com.ninthbalcony.pushuprpg.utils.AppStrings

@Composable
fun RewardedAdDialog(
    title: String,
    description: String,
    rewardText: String,
    language: String,
    onWatchAd: () -> Unit,
    onDecline: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bgResId = remember { context.resources.getIdentifier("bg_fight_3", "drawable", context.packageName) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(DarkCard, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (bgResId != 0) Image(
                painter = painterResource(id = bgResId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.25f
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "${AppStrings.t(language, "reward_label")} $rewardText",
                    fontSize = 16.sp,
                    color = UncommonColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDecline, modifier = Modifier.weight(1f)) {
                        Text(AppStrings.t(language, "btn_skip"), color = TextSecondary)
                    }
                    Button(
                        onClick = onWatchAd,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen)
                    ) {
                        Text(AppStrings.t(language, "ad_reward_title"), color = Color.White)
                    }
                }
            }
        }
    }
}
