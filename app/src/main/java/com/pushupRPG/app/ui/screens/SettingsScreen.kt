package com.pushupRPG.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pushupRPG.app.ui.theme.*
import com.pushupRPG.app.ui.GameViewModel // ИСПРАВЛЕН ИМПОРТ
import com.pushupRPG.app.utils.NotificationScheduler

@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    // ИСПРАВЛЕНО: добавлено initial = null
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val language = gameState?.language ?: "en"

    var showRenameDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(gameState?.playerName ?: "") }

    // Диалог переименования
    if (showRenameDialog) {
        RenameDialog(
            currentName = nameInput,
            language = language,
            onConfirm = { newName ->
                if (newName.isNotBlank()) {
                    viewModel.updatePlayerName(newName.trim())
                    nameInput = newName.trim()
                }
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    // Диалог сброса прогресса
    if (showResetDialog) {
        ResetConfirmDialog(
            language = language,
            onConfirm = {
                showResetDialog = false
                viewModel.resetProgress {
                    onBack()
                }
            },
            onDismiss = { showResetDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // --- Топбар ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = if (language == "ru") "Настройки" else "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            /// --- Персонаж ---
            SettingsSection(
                title = if (language == "ru") "Персонаж" else "Character"
            ) {
                SettingsRow(
                    label = if (language == "ru") "Имя персонажа" else "Character name",
                    value = gameState?.playerName ?: "",
                    onClick = { showRenameDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (language == "ru") "Аватар героя" else "Hero avatar",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("hero_1", "hero_2", "hero_3").forEach { avatarId ->
                        val isSelected = gameState?.heroAvatar == avatarId
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val resId = context.resources.getIdentifier(
                            avatarId, "drawable", context.packageName
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(
                                    if (isSelected) OrangeAccent.copy(alpha = 0.2f)
                                    else DarkSurfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) OrangeAccent else TextMuted,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.updateHeroAvatar(avatarId) }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (resId != 0) {
                                Image(
                                    painter = androidx.compose.ui.res.painterResource(id = resId),
                                    contentDescription = avatarId,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }

            // --- Язык ---
            SettingsSection(
                title = if (language == "ru") "Язык" else "Language"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LanguageButton(
                        label = "English",
                        isSelected = language == "en",
                        onClick = { viewModel.updateLanguage("en") },
                        modifier = Modifier.weight(1f)
                    )
                    LanguageButton(
                        label = "Русский",
                        isSelected = language == "ru",
                        onClick = { viewModel.updateLanguage("ru") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- Уведомления ---
            SettingsSection(
                title = if (language == "ru") "Уведомления" else "Notifications"
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                var notificationsEnabled by remember {
                    val prefs = context.getSharedPreferences("pushup_prefs", android.content.Context.MODE_PRIVATE)
                    mutableStateOf(prefs.getBoolean("notifications_enabled", true))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (language == "ru")
                                "Ежедневные напоминания"
                            else
                                "Daily reminders",
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = if (language == "ru")
                                "В 10:00 и 20:00"
                            else
                                "At 10:00 and 20:00",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                notificationsEnabled = true
                                val prefs = context.getSharedPreferences(
                                    "pushup_prefs", android.content.Context.MODE_PRIVATE
                                )
                                prefs.edit().putBoolean("notifications_enabled", true).apply()
                                NotificationScheduler.scheduleDailyNotifications(context)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (notificationsEnabled) ButtonGreen else ButtonGray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Text(
                                text = if (language == "ru") "Вкл" else "On",
                                fontSize = 13.sp,
                                color = if (notificationsEnabled) Color.White else TextMuted
                            )
                        }
                        Button(
                            onClick = {
                                notificationsEnabled = false
                                val prefs = context.getSharedPreferences(
                                    "pushup_prefs", android.content.Context.MODE_PRIVATE
                                )
                                prefs.edit().putBoolean("notifications_enabled", false).apply()
                                NotificationScheduler.cancelAll(context)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!notificationsEnabled) ButtonRed else ButtonGray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Text(
                                text = if (language == "ru") "Выкл" else "Off",
                                fontSize = 13.sp,
                                color = if (!notificationsEnabled) Color.White else TextMuted
                            )
                        }
                    }
                }
            }

            // --- Информация ---
            SettingsSection(
                title = if (language == "ru") "Информация" else "Information"
            ) {
                gameState?.let { state ->
                    InfoRow(
                        label = if (language == "ru") "Уровень" else "Level",
                        value = "${state.playerLevel}"
                    )
                    InfoRow(
                        label = if (language == "ru") "Всего отжиманий" else "Total push-ups",
                        value = "${state.totalPushUpsAllTime}"
                    )
                    InfoRow(
                        label = if (language == "ru") "Монстров убито" else "Monsters killed",
                        value = "${state.monstersKilled}"
                    )
                    InfoRow(
                        label = if (language == "ru") "Версия" else "Version",
                        value = "1.0.0"
                    )
                }
            }

            // --- Опасная зона ---
            SettingsSection(
                title = if (language == "ru") "Опасная зона" else "Danger zone",
                titleColor = HpBarLow
            ) {
                Button(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonRed
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (language == "ru")
                            "Сбросить прогресс"
                        else
                            "Reset progress",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (language == "ru")
                        "Это действие необратимо. Весь прогресс будет удалён."
                    else
                        "This action is irreversible. All progress will be lost.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

// --- Секция настроек ---
@Composable
fun SettingsSection(
    title: String,
    titleColor: Color = OrangeAccent,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

// --- Строка настройки ---
@Composable
fun SettingsRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = TextPrimary
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                fontSize = 15.sp,
                color = TextSecondary
            )
            Text(
                text = "›",
                fontSize = 20.sp,
                color = TextMuted
            )
        }
    }
}

// --- Строка информации ---
@Composable
fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

// --- Кнопка языка ---
@Composable
fun LanguageButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                if (isSelected) OrangeAccent else DarkSurfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) OrangeAccent else TextMuted,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}

// --- Диалог переименования ---
@Composable
fun RenameDialog(
    currentName: String,
    language: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Text(
                text = if (language == "ru") "Имя персонажа" else "Character name",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 20) text = it },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = OrangeAccent
                ),
                placeholder = {
                    Text(
                        text = if (language == "ru") "Введи имя..." else "Enter name...",
                        color = TextMuted
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSecondary
                    )
                ) {
                    Text(if (language == "ru") "Отмена" else "Cancel")
                }
                Button(
                    onClick = { onConfirm(text) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeAccent
                    )
                ) {
                    Text(
                        if (language == "ru") "Сохранить" else "Save",
                        color = Color.White
                    )
                }
            }
        }
    }
}

// --- Диалог сброса ---
@Composable
fun ResetConfirmDialog(
    language: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "⚠️", fontSize = 48.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (language == "ru")
                    "Сбросить прогресс?"
                else
                    "Reset progress?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HpBarLow
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (language == "ru")
                    "Весь прогресс, предметы и статистика будут удалены навсегда."
                else
                    "All progress, items and statistics will be permanently deleted.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSecondary
                    )
                ) {
                    Text(if (language == "ru") "Отмена" else "Cancel")
                }
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonRed
                    )
                ) {
                    Text(
                        if (language == "ru") "Сбросить" else "Reset",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}