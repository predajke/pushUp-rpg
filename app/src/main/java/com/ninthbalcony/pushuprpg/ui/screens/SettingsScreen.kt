package com.ninthbalcony.pushuprpg.ui.screens

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import android.app.Activity
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.LocalActivity
import com.ninthbalcony.pushuprpg.ui.theme.*
import com.ninthbalcony.pushuprpg.ui.GameViewModel // ИСПРАВЛЕН ИМПОРТ
import com.ninthbalcony.pushuprpg.utils.AppStrings
import com.ninthbalcony.pushuprpg.utils.AvatarSystem
import com.ninthbalcony.pushuprpg.utils.NotificationScheduler
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.ninthbalcony.pushuprpg.ui.preview.FakeGameRepository

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
    var weightInput by remember(gameState?.bodyWeightKg) {
        mutableStateOf(
            if ((gameState?.bodyWeightKg ?: 0f) > 0f) gameState!!.bodyWeightKg.toInt().toString() else ""
        )
    }
    var cheatInput by remember { mutableStateOf("") }
    var showCheatHelp by remember { mutableStateOf(false) }
    val cheatFeedback by viewModel.cheatFeedback.collectAsState()

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

    if (showCheatHelp) {
        AlertDialog(
            onDismissRequest = { showCheatHelp = false },
            title = { Text("Dev Console", color = TextPrimary) },
            text = {
                Text(
                    text = "give lvl <1-49>\ngive teeth <n>\ngive item <id>\ngive items\ngive spins <n>\ngive hp",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showCheatHelp = false }) { Text("OK", color = OrangeAccent) }
            },
            containerColor = DarkSurface
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        ScreenBackground("bg_settings_overall")
    Column(modifier = Modifier.fillMaxSize()) {
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
                text = AppStrings.t(language, "settings"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            /// --- Персонаж ---
            SettingsSection(
                title = AppStrings.t(language, "sec_character")
            ) {
                SettingsRow(
                    label = AppStrings.t(language, "char_name"),
                    value = gameState?.playerName ?: "",
                    onClick = { showRenameDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = AppStrings.t(language, "hero_avatar"),
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                val context = LocalContext.current
                val currentGender = gameState?.playerGender ?: "male"
                val unlockedIds = AvatarSystem.getUnlocked(gameState?.unlockedAvatarIds ?: "")

                // Gender toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("male" to "♂ Male", "female" to "♀ Female").forEach { (g, label) ->
                        val selected = currentGender == g
                        Button(
                            onClick = { viewModel.updatePlayerGender(g) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) OrangeAccent else DarkSurfaceVariant,
                                contentColor = if (selected) Color.Black else TextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Avatar grid (4 columns × 2 rows)
                AvatarSystem.AVATARS.chunked(4).forEach { rowAvatars ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowAvatars.forEach { def ->
                            val isUnlocked = def.id in unlockedIds
                            val isSelected = gameState?.heroAvatar == def.id
                            val resId = com.ninthbalcony.pushuprpg.ui.util.rememberAvatarResId(def.id, currentGender)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(
                                        when {
                                            isSelected -> OrangeAccent.copy(alpha = 0.2f)
                                            isUnlocked -> DarkSurfaceVariant
                                            else -> DarkSurfaceVariant.copy(alpha = 0.5f)
                                        },
                                        RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) OrangeAccent else if (isUnlocked) TextMuted else TextMuted.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .then(
                                        if (isUnlocked) Modifier.clickable { viewModel.updateHeroAvatar(def.id) }
                                        else Modifier
                                    )
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUnlocked) {
                                    if (resId != 0) {
                                        Image(
                                            painter = androidx.compose.ui.res.painterResource(id = resId),
                                            contentDescription = def.id,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("🔒", fontSize = 18.sp)
                                        val cond = if (language == "ru") def.conditionRu else def.conditionEn
                                        Text(
                                            text = cond,
                                            fontSize = 8.sp,
                                            color = TextMuted,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                        // Fill remaining cells if row has < 4
                        repeat(4 - rowAvatars.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (language == "ru") "⚖️ Вес тела (кг):" else "⚖️ Body weight (kg):",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { v ->
                            if (v.length <= 3 && v.all { it.isDigit() }) {
                                weightInput = v
                                v.toFloatOrNull()?.let { viewModel.updateBodyWeight(it) }
                            }
                        },
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeAccent,
                            unfocusedBorderColor = TextMuted,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            // --- Язык ---
            SettingsSection(
                title = AppStrings.t(language, "sec_language")
            ) {
                val languages = listOf(
                    "en" to "🇺🇸", "ru" to "🇷🇺", "es" to "🇪🇸",
                    "fr" to "🇫🇷", "de" to "🇩🇪", "pt" to "🇧🇷"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    languages.forEach { (code, flag) ->
                        LanguageButton(
                            label = flag,
                            isSelected = language == code,
                            onClick = { viewModel.updateLanguage(code) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- Уведомления ---
            SettingsSection(
                title = AppStrings.t(language, "sec_notifications")
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
                            text = AppStrings.t(language, "notif_label"),
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = AppStrings.t(language, "notif_time"),
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
                            modifier = Modifier.width(72.dp).height(36.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = AppStrings.t(language, "btn_on"),
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
                            modifier = Modifier.width(72.dp).height(36.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = AppStrings.t(language, "btn_off"),
                                fontSize = 13.sp,
                                color = if (!notificationsEnabled) Color.White else TextMuted
                            )
                        }
                    }
                }
            }

            // --- Звук и вибрация ---
            SettingsSection(title = if (language == "ru") "🔊 Звук и вибрация" else "🔊 Sound & Vibration") {
                val context = LocalContext.current
                val prefs = remember { context.getSharedPreferences("pushup_prefs", android.content.Context.MODE_PRIVATE) }
                var soundsEnabled by remember { mutableStateOf(prefs.getBoolean("sounds_enabled", true)) }
                var vibrationEnabled by remember { mutableStateOf(prefs.getBoolean("vibration_enabled", true)) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (language == "ru") "Звуки и музыка" else "Sound & Music", fontSize = 15.sp, color = TextPrimary)
                    Switch(
                        checked = soundsEnabled,
                        onCheckedChange = {
                            soundsEnabled = it
                            prefs.edit().putBoolean("sounds_enabled", it).apply()
                            if (!it) com.ninthbalcony.pushuprpg.utils.SoundManager.stopMusic()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = OrangeAccent, checkedTrackColor = OrangeAccent.copy(alpha = 0.4f))
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (language == "ru") "Вибрация" else "Vibration", fontSize = 15.sp, color = TextPrimary)
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = {
                            vibrationEnabled = it
                            prefs.edit().putBoolean("vibration_enabled", it).apply()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = OrangeAccent, checkedTrackColor = OrangeAccent.copy(alpha = 0.4f))
                    )
                }
            }

            // --- Информация ---
            SettingsSection(
                title = AppStrings.t(language, "sec_info")
            ) {
                InfoRow(
                    label = AppStrings.t(language, "info_version"),
                    value = "1.0.0"
                )
            }

            // --- Dev Console ---
            SettingsSection(title = "🛠 Dev Console") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cheatInput,
                        onValueChange = { cheatInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("give lvl 49", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeAccent,
                            unfocusedBorderColor = TextMuted,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = OrangeAccent
                        )
                    )
                    IconButton(onClick = { viewModel.executeCheat(cheatInput); cheatInput = "" }) {
                        Text("▶", fontSize = 18.sp, color = OrangeAccent)
                    }
                    IconButton(onClick = { showCheatHelp = true }) {
                        Text("?", fontSize = 16.sp, color = TextMuted)
                    }
                }
                if (cheatFeedback.isNotEmpty()) {
                    Text(
                        text = cheatFeedback,
                        color = if (cheatFeedback.startsWith("❌")) ButtonRed else HpBarFull,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // --- Выход ---
            val activity = LocalActivity.current
            SettingsSection(
                title = AppStrings.t(language, "sec_exit")
            ) {
                Button(
                    onClick = {
                        activity?.finishAndRemoveTask()
                        android.os.Process.killProcess(android.os.Process.myPid())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = AppStrings.t(language, "btn_exit_game"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- Опасная зона ---
            SettingsSection(
                title = AppStrings.t(language, "sec_danger"),
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
                        text = AppStrings.t(language, "btn_reset_progress"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = AppStrings.t(language, "confirm_reset_warn"),
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    } // Column
    } // Box (фон)
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
            fontSize = 20.sp,
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
                text = AppStrings.t(language, "char_name"),
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
                        text = AppStrings.t(language, "input_name"),
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
                    Text(AppStrings.t(language, "btn_cancel"))
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
                        AppStrings.t(language, "btn_save"),
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
                text = AppStrings.t(language, "confirm_reset_title"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HpBarLow
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = AppStrings.t(language, "confirm_reset_msg"),
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
                    Text(AppStrings.t(language, "btn_cancel"))
                }
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonRed
                    )
                ) {
                    Text(
                        AppStrings.t(language, "btn_reset_progress"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 920)
@Composable
private fun SettingsScreenPreview() {
    val vm = remember { GameViewModel(FakeGameRepository()) }
    SettingsScreen(viewModel = vm, onBack = {})
}
