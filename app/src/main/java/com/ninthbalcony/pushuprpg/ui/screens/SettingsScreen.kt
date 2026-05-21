package com.ninthbalcony.pushuprpg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.ninthbalcony.pushuprpg.BuildConfig
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.LocalActivity
import com.ninthbalcony.pushuprpg.ui.theme.*
import com.ninthbalcony.pushuprpg.ui.GameViewModel
import com.ninthbalcony.pushuprpg.utils.AppStrings
import com.ninthbalcony.pushuprpg.utils.NotificationScheduler
import androidx.compose.ui.tooling.preview.Preview
import com.ninthbalcony.pushuprpg.ui.preview.FakeGameRepository

@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val language = gameState?.language ?: "en"
    var selectedTab by remember { mutableStateOf(0) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(gameState?.playerName ?: "") }

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

    if (showResetDialog) {
        ResetConfirmDialog(
            language = language,
            onConfirm = {
                showResetDialog = false
                viewModel.resetProgress { onBack() }
            },
            onDismiss = { showResetDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        DeleteAccountConfirmDialog(
            language = language,
            onConfirm = {
                showDeleteAccountDialog = false
                viewModel.deleteAccountAndData { _ -> onBack() }
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        ScreenBackground("bg_settings_overall")
        Column(modifier = Modifier.fillMaxSize()) {
            // --- Top bar ---
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
                    text = if (selectedTab == 0)
                        AppStrings.t(language, "profile_tab")
                    else
                        AppStrings.t(language, "settings"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // --- Tabs ---
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = OrangeAccent,
            ) {
                listOf(
                    AppStrings.t(language, "profile_tab"),
                    AppStrings.t(language, "settings"),
                ).forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == i) OrangeAccent else TextSecondary,
                            )
                        }
                    )
                }
            }

            // --- Tab content ---
            val gs = gameState
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                if (gs == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OrangeAccent)
                    }
                } else when (selectedTab) {
                    0 -> ProfileTab(
                        viewModel = viewModel,
                        state = gs,
                        onRequestRename = { showRenameDialog = true },
                    )
                    1 -> SettingsTabContent(
                        viewModel = viewModel,
                        gameState = gs,
                        onResetProgressClick = { showResetDialog = true },
                        onDeleteAccountClick = { showDeleteAccountDialog = true },
                    )
                }
            }
        }
    }
}

// =================================================================
// Settings tab — language, notifications, sound, vibration, body
// weight, dev console, exit/log out, reset progress.
// =================================================================
@Composable
private fun SettingsTabContent(
    viewModel: GameViewModel,
    gameState: com.ninthbalcony.pushuprpg.data.db.GameStateEntity,
    onResetProgressClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
) {
    val language = gameState.language
    var cheatInput by remember { mutableStateOf("") }
    var showCheatHelp by remember { mutableStateOf(false) }
    val cheatFeedback by viewModel.cheatFeedback.collectAsState()
    var weightInput by remember(gameState.bodyWeightKg) {
        mutableStateOf(
            if (gameState.bodyWeightKg > 0f) gameState.bodyWeightKg.toInt().toString() else ""
        )
    }

    if (showCheatHelp) {
        AlertDialog(
            onDismissRequest = { showCheatHelp = false },
            title = { Text("Dev Console", color = TextPrimary) },
            text = {
                Text(
                    text = "give xp <n>\ngive lvl <1-49>\ngive teeth <n>\ngive item <id>\ngive items\ngive spins <n>\ngive hp\nevent <1-11>  (6,9,10,11 = night)\nevent none",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showCheatHelp = false }) { Text(AppStrings.t(language, "btn_ok"), color = OrangeAccent) }
            },
            containerColor = DarkSurface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- Language ---
        SettingsSection(title = AppStrings.t(language, "sec_language")) {
            val languages = listOf(
                "en" to "🇺🇸", "ru" to "🇷🇺", "es" to "🇪🇸",
                "fr" to "🇫🇷", "de" to "🇩🇪", "pt" to "🇧🇷"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
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

        // --- Notifications ---
        SettingsSection(title = AppStrings.t(language, "sec_notifications")) {
            val context = LocalContext.current
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
                    Text(AppStrings.t(language, "notif_label"), fontSize = 15.sp, color = TextPrimary)
                    Text(AppStrings.t(language, "notif_time"), fontSize = 12.sp, color = TextMuted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            notificationsEnabled = true
                            context.getSharedPreferences("pushup_prefs", android.content.Context.MODE_PRIVATE)
                                .edit().putBoolean("notifications_enabled", true).apply()
                            NotificationScheduler.scheduleDailyNotifications(context)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (notificationsEnabled) ButtonGreen else ButtonGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(56.dp).height(26.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text(
                            AppStrings.t(language, "btn_on"),
                            fontSize = 14.sp,
                            color = if (notificationsEnabled) Color.White else TextMuted
                        )
                    }
                    Button(
                        onClick = {
                            notificationsEnabled = false
                            context.getSharedPreferences("pushup_prefs", android.content.Context.MODE_PRIVATE)
                                .edit().putBoolean("notifications_enabled", false).apply()
                            NotificationScheduler.cancelAll(context)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!notificationsEnabled) ButtonRed else ButtonGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(56.dp).height(26.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text(
                            AppStrings.t(language, "btn_off"),
                            fontSize = 14.sp,
                            color = if (!notificationsEnabled) Color.White else TextMuted
                        )
                    }
                }
            }
        }

        // --- Sound & Vibration ---
        SettingsSection(title = AppStrings.t(language, "sound_vibration_section")) {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("pushup_prefs", android.content.Context.MODE_PRIVATE) }
            var soundsEnabled by remember { mutableStateOf(prefs.getBoolean("sounds_enabled", true)) }
            var vibrationEnabled by remember { mutableStateOf(prefs.getBoolean("vibration_enabled", true)) }

            Row(
                modifier = Modifier.fillMaxWidth().height(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(AppStrings.t(language, "sound_music_label"), fontSize = 15.sp, color = TextPrimary)
                Switch(
                    checked = soundsEnabled,
                    onCheckedChange = {
                        soundsEnabled = it
                        prefs.edit().putBoolean("sounds_enabled", it).apply()
                        if (!it) com.ninthbalcony.pushuprpg.utils.SoundManager.stopMusic()
                    },
                    modifier = Modifier.scale(0.8f),
                    colors = SwitchDefaults.colors(checkedThumbColor = OrangeAccent, checkedTrackColor = OrangeAccent.copy(alpha = 0.4f))
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().height(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(AppStrings.t(language, "vibration_label"), fontSize = 15.sp, color = TextPrimary)
                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = {
                        vibrationEnabled = it
                        prefs.edit().putBoolean("vibration_enabled", it).apply()
                    },
                    modifier = Modifier.scale(0.8f),
                    colors = SwitchDefaults.colors(checkedThumbColor = OrangeAccent, checkedTrackColor = OrangeAccent.copy(alpha = 0.4f))
                )
            }
        }

        // --- Body weight ---
        SettingsSection(title = AppStrings.t(language, "stats_section_label")) {
            // Row.height(28.dp) был слишком мал для OutlinedTextField (min content
            // height ~56dp) — текст обрезался изнутри, и пользователь не видел
            // ни введённое значение, ни сохранённое. Убираем фиксированную высоту.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = AppStrings.t(language, "body_weight_label"),
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                BasicTextField(
                    value = weightInput,
                    onValueChange = { v ->
                        if (v.length <= 3 && v.all { it.isDigit() }) {
                            weightInput = v
                            v.toFloatOrNull()?.let { viewModel.updateBodyWeight(it) }
                        }
                    },
                    modifier = Modifier
                        .width(64.dp)
                        .height(34.dp)
                        .border(1.dp, TextMuted, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(OrangeAccent),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = TextPrimary
                    ),
                    decorationBox = { inner ->
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            inner()
                        }
                    }
                )
            }
        }

        // --- Info ---
        SettingsSection(title = AppStrings.t(language, "sec_info")) {
            // BuildConfig.VERSION_NAME — единственный источник правды, тянется из
            // android.defaultConfig.versionName в build.gradle.kts. Раньше был
            // хардкод "0.9.1" который зависал на старой версии при бампах.
            InfoRow(label = AppStrings.t(language, "info_version"), value = BuildConfig.VERSION_NAME)
        }

        // --- Dev Console (only in debug builds, hidden in release) ---
        if (BuildConfig.DEBUG) SettingsSection(title = "🛠 Dev Console") {
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

        // --- Log out / Exit game ---
        val activity = LocalActivity.current
        SettingsSection(title = AppStrings.t(language, "sec_exit")) {
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
                    text = AppStrings.t(language, "sign_out"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- Danger zone ---
        SettingsSection(title = AppStrings.t(language, "sec_danger"), titleColor = HpBarLow) {
            Button(
                onClick = onResetProgressClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    AppStrings.t(language, "btn_reset_progress"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(AppStrings.t(language, "confirm_reset_warn"), fontSize = 11.sp, color = TextMuted)

            Spacer(Modifier.height(16.dp))

            // Delete account & data — отдельная кнопка для GDPR / Google Play
            // Data Safety requirements. В отличие от Reset Progress, чистит
            // ещё и облако (users/{uid}, leaderboard/{uid}, friendCodes, auth).
            Button(
                onClick = onDeleteAccountClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = HpBarLow),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    AppStrings.t(language, "btn_delete_account"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(AppStrings.t(language, "delete_account_warn"), fontSize = 11.sp, color = TextMuted)
        }
    }
}

// =================================================================
// Reusable section / row composables (unchanged from v0.9.1)
// =================================================================

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

@Composable
fun SettingsRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 15.sp, color = TextPrimary)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = value, fontSize = 16.sp, color = TextSecondary)
            Text(text = "›", fontSize = 20.sp, color = TextMuted)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Text(value, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun LanguageButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(if (isSelected) OrangeAccent else DarkSurfaceVariant, RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = if (isSelected) OrangeAccent else TextMuted, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 20.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}

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
                AppStrings.t(language, "char_name"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(16.dp))
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
                placeholder = { Text(AppStrings.t(language, "input_name"), color = TextMuted) }
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) { Text(AppStrings.t(language, "btn_cancel")) }
                Button(
                    onClick = { onConfirm(text) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) { Text(AppStrings.t(language, "btn_save"), color = Color.White) }
            }
        }
    }
}

@Composable
fun ResetConfirmDialog(language: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⚠️", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                AppStrings.t(language, "confirm_reset_title"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HpBarLow
            )
            Spacer(Modifier.height(8.dp))
            Text(
                AppStrings.t(language, "confirm_reset_msg"),
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) { Text(AppStrings.t(language, "btn_cancel")) }
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonRed)
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

@Composable
fun DeleteAccountConfirmDialog(language: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🗑️", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                AppStrings.t(language, "confirm_delete_title"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HpBarLow
            )
            Spacer(Modifier.height(8.dp))
            Text(
                AppStrings.t(language, "confirm_delete_msg"),
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) { Text(AppStrings.t(language, "btn_cancel")) }
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HpBarLow)
                ) {
                    Text(
                        AppStrings.t(language, "btn_delete_account"),
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
