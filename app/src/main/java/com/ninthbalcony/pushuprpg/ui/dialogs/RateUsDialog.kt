package com.ninthbalcony.pushuprpg.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
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
import com.ninthbalcony.pushuprpg.ui.theme.DarkSurface
import com.ninthbalcony.pushuprpg.ui.theme.GoldAccent
import com.ninthbalcony.pushuprpg.ui.theme.OrangeAccent
import com.ninthbalcony.pushuprpg.ui.theme.TextMuted
import com.ninthbalcony.pushuprpg.ui.theme.TextPrimary

/**
 * Dark-theme Rate Us popup. Три кнопки стэкаются вертикально на полную
 * ширину — длинные локализованные тексты («Напомнить позже» / «Никогда
 * не спрашивать» / «Erinnere mich später») больше не обрезаются.
 *
 * Совпадает по стилю с DailyRewardDialog / StreakRewardDialog: DarkSurface
 * background, полупрозрачный bg_record_popup overlay, скруглённые углы.
 */
@Composable
fun RateUsDialog(
    onRate: () -> Unit,
    onRemindLater: () -> Unit,
    onNeverAsk: () -> Unit,
    onDismiss: () -> Unit,
    title: String = "Enjoying PushUpRPG?",
    description: String = "If you're having a great time, please take a moment to rate the app. Your feedback helps us improve!",
    buttonRate: String = "Rate Now",
    buttonReminder: String = "Remind Later",
    buttonNever: String = "Never Ask"
) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_record_popup", "drawable", context.packageName)
    }
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(DarkSurface, RoundedCornerShape(16.dp))
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
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(18.dp))

                // Primary action — Rate Now (зелёный, как «положительное действие»).
                Button(
                    onClick = onRate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(buttonRate, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))

                // Secondary — Remind Later (вторая по приоритету, оранжевый акцент).
                Button(
                    onClick = onRemindLater,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(buttonReminder, color = Color.White, fontSize = 13.sp)
                }
                Spacer(Modifier.height(4.dp))

                // Tertiary — Never Ask (мелкая ссылка, серая).
                TextButton(
                    onClick = onNeverAsk,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(buttonNever, color = TextMuted, fontSize = 12.sp)
                }
            }
        }
    }
}
