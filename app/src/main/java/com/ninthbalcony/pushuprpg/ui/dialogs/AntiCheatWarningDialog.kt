package com.ninthbalcony.pushuprpg.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = AppStrings.t(language, "anticheat_title"),
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = AppStrings.t(language, "anticheat_body"),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "${AppStrings.t(language, "anticheat_cooldown")} ${remainingSeconds.value}s",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = AppStrings.t(language, "anticheat_footer"),
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(AppStrings.t(language, "btn_ok"))
            }
        },
        modifier = Modifier.fillMaxWidth(0.9f)
    )
}
