package com.pushupRPG.app.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Canvas
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pushupRPG.app.managers.OnboardingManager
import com.pushupRPG.app.ui.theme.DarkBackground
import com.pushupRPG.app.ui.theme.OrangeAccent

@Composable
fun HighlightTourGuideDialog(
    currentStep: Int,
    onboardingManager: OnboardingManager,
    language: String,
    targetRect: Rect = Rect.Zero,
    statusBarHeightPx: Float = 0f,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onComplete: () -> Unit
) {
    if (currentStep >= OnboardingManager.TOTAL_STEPS) {
        android.util.Log.d("HighlightTour", "Dialog skipped - step=$currentStep")
        return
    }

    // If no target rect provided, use a default rect in center
    val displayRect = if (targetRect == Rect.Zero) {
        // Default rect in center of screen (will be visible in middle)
        Rect(left = 200f, top = 400f, right = 880f, bottom = 800f)
    } else {
        targetRect
    }

    android.util.Log.d("HighlightTour", "Showing dialog at step=$currentStep, rect=$targetRect")

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
                .pointerInput(Unit) { }
        ) {
            // Canvas overlay with transparent cutout
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            ) {
                val padding = 16f
                val cornerRadius = 24f

                // Draw semi-transparent background over entire screen
                drawRect(
                    color = Color.Black.copy(alpha = 0.75f),
                    size = size
                )

                // Adjust for status bar: boundsInWindow() is relative to app window,
                // but Dialog canvas draws from physical top of screen (includes status bar)
                val left = displayRect.left - padding
                val top = displayRect.top - statusBarHeightPx - padding
                val right = displayRect.right + padding
                val bottom = displayRect.bottom - statusBarHeightPx + padding

                val cutoutPath = Path().apply {
                    moveTo(left + cornerRadius, top)
                    lineTo(right - cornerRadius, top)
                    quadraticBezierTo(right, top, right, top + cornerRadius)
                    lineTo(right, bottom - cornerRadius)
                    quadraticBezierTo(right, bottom, right - cornerRadius, bottom)
                    lineTo(left + cornerRadius, bottom)
                    quadraticBezierTo(left, bottom, left, bottom - cornerRadius)
                    lineTo(left, top + cornerRadius)
                    quadraticBezierTo(left, top, left + cornerRadius, top)
                    close()
                }

                // Punch a real transparent hole using BlendMode.Clear
                drawPath(
                    path = cutoutPath,
                    color = Color.Transparent,
                    blendMode = BlendMode.Clear
                )

                // Orange glow border around the cutout
                drawPath(
                    path = cutoutPath,
                    color = OrangeAccent,
                    style = Stroke(width = 3f)
                )
            }

            // Info box: bottom for steps 0-3, top for steps 4-5 (Logs/Quests are near bottom)
            val infoAlignment = if (currentStep >= 4) Alignment.TopCenter else Alignment.BottomCenter
            Column(
                modifier = Modifier
                    .align(infoAlignment)
                    .fillMaxWidth()
                    .padding(24.dp)
                    .background(
                        color = DarkBackground,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                // Title
                Text(
                    text = onboardingManager.getStepTitle(currentStep, language),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Description
                val description = onboardingManager.getStepDescription(currentStep, language)
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Left,
                    modifier = Modifier.padding(bottom = 16.dp),
                    lineHeight = 20.sp
                )

                // Progress dots
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
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                        if (index < OnboardingManager.TOTAL_STEPS - 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            com.pushupRPG.app.utils.AppStrings.t(language, "onboard_skip"),
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

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
                                com.pushupRPG.app.utils.AppStrings.t(language, "btn_continue")
                            else
                                com.pushupRPG.app.utils.AppStrings.t(language, "onboard_next"),
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
