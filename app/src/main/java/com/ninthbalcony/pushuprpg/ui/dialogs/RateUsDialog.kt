package com.ninthbalcony.pushuprpg.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = description,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onRate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(buttonRate)
            }
        },
        dismissButton = {
            Column {
                TextButton(
                    onClick = onRemindLater,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp)
                ) {
                    Text(buttonReminder)
                }
                TextButton(
                    onClick = onNeverAsk,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(buttonNever, color = Color.Gray)
                }
            }
        },
        modifier = Modifier.fillMaxWidth(0.9f)
    )
}
