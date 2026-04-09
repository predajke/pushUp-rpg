package com.ninthbalcony.pushuprpg.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ninthbalcony.pushuprpg.managers.OnboardingManager
import com.ninthbalcony.pushuprpg.ui.theme.DarkBackground
import com.ninthbalcony.pushuprpg.ui.theme.OrangeAccent

@Composable
fun CenterOnboardingDialog(
    currentStep: Int,
    onboardingManager: OnboardingManager,
    language: String,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onComplete: () -> Unit
) {
    android.util.Log.d("CenterOnboarding", "Dialog composable called with step=$currentStep, language=$language")

    if (currentStep >= OnboardingManager.TOTAL_STEPS) {
        android.util.Log.d("CenterOnboarding", "Dialog returning early - step >= TOTAL")
        return
    }

    // Get icon for each step
    val stepIcon: ImageVector = when (currentStep) {
        0 -> Icons.Default.FavoriteBorder     // Total Pushups
        1 -> Icons.Default.Home               // Inventory
        2 -> Icons.Default.ShoppingCart       // Shop
        3 -> Icons.Default.Info               // Battle
        4 -> Icons.Default.Info               // Logs
        5 -> Icons.Default.Settings           // Quests
        else -> Icons.Default.FavoriteBorder
    }

    Dialog(
        onDismissRequest = onSkip,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top spacing
            Spacer(modifier = Modifier.height(40.dp))

            // Main content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = OrangeAccent.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = stepIcon,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = OrangeAccent
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Title
                val title = onboardingManager.getStepTitle(currentStep, language)
                android.util.Log.d("CenterOnboarding", "Title: $title")
                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Description
                val description = onboardingManager.getStepDescription(currentStep, language)
                android.util.Log.d("CenterOnboarding", "Description: ${description.take(50)}...")
                Text(
                    text = description,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    lineHeight = 24.sp
                )
            }

            // Bottom content box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = DarkBackground.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                // Progress indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(OnboardingManager.TOTAL_STEPS) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (index == currentStep) OrangeAccent else Color.Gray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                        if (index < OnboardingManager.TOTAL_STEPS - 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }

                // Step counter
                Text(
                    text = "${currentStep + 1}/${OnboardingManager.TOTAL_STEPS}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip button
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            com.ninthbalcony.pushuprpg.utils.AppStrings.t(language, "onboard_skip"),
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Next/Complete button
                    Button(
                        onClick = {
                            if (currentStep + 1 >= OnboardingManager.TOTAL_STEPS) {
                                onComplete()
                            } else {
                                onNext()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangeAccent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            if (currentStep + 1 >= OnboardingManager.TOTAL_STEPS)
                                com.ninthbalcony.pushuprpg.utils.AppStrings.t(language, "btn_continue")
                            else
                                com.ninthbalcony.pushuprpg.utils.AppStrings.t(language, "onboard_next"),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        }
    }
}
