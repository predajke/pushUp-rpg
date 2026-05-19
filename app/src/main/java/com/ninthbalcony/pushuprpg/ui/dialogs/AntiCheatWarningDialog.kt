package com.ninthbalcony.pushuprpg.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ninthbalcony.pushuprpg.ui.theme.DarkCard
import com.ninthbalcony.pushuprpg.ui.theme.TextPrimary
import com.ninthbalcony.pushuprpg.ui.theme.TextSecondary
import com.ninthbalcony.pushuprpg.utils.AppStrings
import kotlinx.coroutines.delay

@Composable
fun AntiCheatWarningDialog(
    remainingCooldownMs: Long,
    language: String,
    onDismiss: () -> Unit
) {
    val remainingSeconds = remember { mutableStateOf(remainingCooldownMs / 1000) }

    LaunchedEffect(Unit) {
        while (remainingSeconds.value > 0) {
            delay(1000)
            remainingSeconds.value -= 1
        }
        onDismiss()
    }

    val context = LocalContext.current
    val bgResId = remember { context.resources.getIdentifier("bg_fight_5", "drawable", context.packageName) }

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
                    text = AppStrings.t(language, "anticheat_title"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = AppStrings.t(language, "anticheat_body"),
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "${AppStrings.t(language, "anticheat_cooldown")} ${remainingSeconds.value}s",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = AppStrings.t(language, "anticheat_footer"),
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(AppStrings.t(language, "btn_ok"))
                }
            }
        }
    }
}
