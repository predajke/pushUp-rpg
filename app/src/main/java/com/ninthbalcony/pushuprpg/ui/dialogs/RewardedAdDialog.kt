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
 * RewardedAdDialog - offer to watch ad for bonus rewards.
 *
 * Usage contexts:
 * - Clover Box: +2 free attempts
 * - Shop: +3 reroll tokens
 * - Daily Quests: +1 quest refresh
 * - Forge: Re-attempt failed merge with same items
 */
@Composable
fun RewardedAdDialog(
    title: String,
    rewardDescription: String,
    onWatchAd: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
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
                        title,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Reward Description
                    Text(
                        rewardDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Ad Icon
                    Text(
                        "📺",
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Instructions
                    Text(
                        "Watch a short ad to claim your reward!",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel Button
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("Skip")
                        }

                        // Watch Ad Button
                        Button(
                            onClick = onWatchAd,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangeAccent
                            )
                        ) {
                            Text("Watch Ad")
                        }
                    }
                }
            }
        }
    )
}
