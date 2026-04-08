package com.pushupRPG.app.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pushupRPG.app.managers.OnboardingManager
import com.pushupRPG.app.ui.theme.DarkBackground
import com.pushupRPG.app.ui.theme.OrangeAccent

@Composable
fun OnboardingDialog(
    currentStep: Int,
    onboardingManager: OnboardingManager,
    language: String,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onComplete: () -> Unit
) {
    if (currentStep >= OnboardingManager.TOTAL_STEPS) {
        return  // Онбординг завершён
    }

    Dialog(
        onDismissRequest = onSkip,
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .background(Color.Transparent),
                colors = CardDefaults.cardColors(
                    containerColor = DarkBackground
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Заголовок
                    Text(
                        text = onboardingManager.getStepTitle(currentStep, language),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeAccent,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Описание
                    Text(
                        text = onboardingManager.getStepDescription(currentStep, language),
                        fontSize = 16.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp),
                        lineHeight = 22.sp
                    )

                    // Прогресс (шаг X из Y)
                    Text(
                        text = "${currentStep + 1}/${OnboardingManager.TOTAL_STEPS}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Кнопки
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Кнопка "Пропустить"
                        TextButton(
                            onClick = onSkip,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                com.pushupRPG.app.utils.AppStrings.t(language, "onboard_skip"),
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }

                        // Кнопка "Дальше"
                        Button(
                            onClick = {
                                if (currentStep + 1 >= OnboardingManager.TOTAL_STEPS) {
                                    onComplete()
                                } else {
                                    onNext()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangeAccent
                            )
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
    )
}
