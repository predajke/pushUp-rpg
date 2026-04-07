package com.ninthbalcony.pushuprpg.ui.dialogs

import android.content.Context
import android.content.Intent
import android.net.Uri
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
 * RateUsDialog - Material Design dialog with 3 buttons:
 * 1. Rate Us → open Play Store
 * 2. Remind Later → set 2-day cooldown
 * 3. Never → disable forever
 */
@Composable
fun RateUsDialog(
    context: Context,
    onRate: () -> Unit,
    onRemindLater: () -> Unit,
    onNeverShow: () -> Unit
) {
    Dialog(
        onDismissRequest = onRemindLater,
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
                    // Title
                    Text(
                        "Enjoying Push Up RPG?",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Description
                    Text(
                        "Help us improve by rating the app!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Rate Us Button
                        Button(
                            onClick = {
                                onRate()
                                openPlayStore(context)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangeAccent
                            )
                        ) {
                            Text("Rate Us ⭐")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Remind Later Button
                        OutlinedButton(
                            onClick = onRemindLater,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Remind Me Later")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Never Show Button
                        TextButton(
                            onClick = onNeverShow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Don't Show Again", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    )
}

private fun openPlayStore(context: Context) {
    val packageName = "com.ninthbalcony.pushuprpg"
    try {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to web version if Play Store app not installed
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
