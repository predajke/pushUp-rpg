package com.ninthbalcony.pushuprpg.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ninthbalcony.pushuprpg.ui.theme.OrangeAccent

/**
 * AntiCheatWarningDialog - warning when user saves too quickly.
 * Shows cooldown timer and blocks further saves.
 */
@Composable
fun AntiCheatWarningDialog(
    secondsToWait: Long,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon/Title
                    Text(
                        "⏱️ Hold On!",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Description
                    Text(
                        "You're saving too quickly. Take a breath and wait a moment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Cooldown Timer
                    Text(
                        "Wait ${secondsToWait}s",
                        style = MaterialTheme.typography.displaySmall,
                        color = OrangeAccent,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    // Explanation
                    Text(
                        "This protects game integrity. Your progress is safe!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // OK Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangeAccent
                        )
                    ) {
                        Text("OK, I'll Wait")
                    }
                }
            }
        }
    )
}
