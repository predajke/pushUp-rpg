package com.ninthbalcony.pushuprpg.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import com.ninthbalcony.pushuprpg.data.repository.LeaderboardEntry
import com.ninthbalcony.pushuprpg.ui.GameViewModel
import com.ninthbalcony.pushuprpg.ui.components.AvatarPickerDialog
import com.ninthbalcony.pushuprpg.ui.components.CountryPickerDialog
import com.ninthbalcony.pushuprpg.ui.components.ClanTagPill
import com.ninthbalcony.pushuprpg.ui.components.ClanTagText
import com.ninthbalcony.pushuprpg.ui.components.clanTagColor
import com.ninthbalcony.pushuprpg.ui.components.CLAN_TAG_COLOR_MAP
import com.ninthbalcony.pushuprpg.ui.theme.*
import com.ninthbalcony.pushuprpg.ui.util.rememberAvatarResId
import com.ninthbalcony.pushuprpg.utils.AppStrings
import com.ninthbalcony.pushuprpg.utils.AvatarSystem
import com.ninthbalcony.pushuprpg.utils.countryToFlag

@Composable
fun ProfileTab(
    viewModel: GameViewModel,
    state: GameStateEntity,
    onRequestRename: () -> Unit,
) {
    val language = state.language
    var showCountryPicker by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var showClanEditor by remember { mutableStateOf(false) }

    if (showCountryPicker) {
        CountryPickerDialog(
            currentCode = state.playerCountry,
            language = language,
            onPick = { code ->
                viewModel.updatePlayerCountry(code)
                showCountryPicker = false
            },
            onDismiss = { showCountryPicker = false },
        )
    }

    if (showAvatarPicker) {
        AvatarPickerDialog(
            currentAvatar = state.heroAvatar,
            currentGender = state.playerGender,
            unlockedIds = AvatarSystem.getUnlocked(state.unlockedAvatarIds),
            language = language,
            onPick = { id ->
                viewModel.updateHeroAvatar(id)
                showAvatarPicker = false
            },
            onDismiss = { showAvatarPicker = false },
        )
    }

    if (showClanEditor) {
        ClanTagEditorDialog(
            currentTag = state.clanTag,
            currentColor = state.clanTagColor,
            language = language,
            onSave = { tag, color ->
                viewModel.updateClanTag(tag, color)
                showClanEditor = false
            },
            onDismiss = { showClanEditor = false },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Header card: flag + clanTag + name + edit ────────────────────
        ProfileHeaderCard(
            state = state,
            onFlagClick = { showCountryPicker = true },
            onClanClick = { showClanEditor = true },
            onEditNameClick = onRequestRename,
        )

        // ── Avatar card ──────────────────────────────────────────────────
        AvatarCard(
            state = state,
            language = language,
            onGenderChange = { viewModel.updatePlayerGender(it) },
            onChangeAvatar = { showAvatarPicker = true },
        )

        // ── Play Games sign-in ──────────────────────────────────────────
        PlayGamesCard(viewModel = viewModel, language = language)

        // ── Friends section ──────────────────────────────────────────────
        FriendsCard(viewModel = viewModel, language = language)
    }
}

@Composable
private fun PlayGamesCard(viewModel: GameViewModel, language: String) {
    val signedIn by viewModel.playGamesSignedIn.collectAsState()
    val playerName by viewModel.playGamesPlayerName.collectAsState()
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Status indicator
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (signedIn) Color(0xFF34C759) else TextMuted),
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (signedIn) {
                    "Play Games"
                } else {
                    AppStrings.t(language, "play_games_not_connected")
                },
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (signedIn) playerName else AppStrings.t(language, "save_progress_desc"),
                color = if (signedIn) Color(0xFF34C759) else TextMuted,
                fontSize = 11.sp,
            )
        }
        if (signedIn) {
            TextButton(onClick = { activity?.let { viewModel.signOutPlayGames(it) } }) {
                Text(AppStrings.t(language, "sign_out"), color = TextMuted, fontSize = 12.sp)
            }
        } else {
            Button(
                onClick = { activity?.let { viewModel.signInPlayGames(it) } },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    AppStrings.t(language, "sign_in"),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// ── Header ───────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeaderCard(
    state: GameStateEntity,
    onFlagClick: () -> Unit,
    onClanClick: () -> Unit,
    onEditNameClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Flag (clickable)
        Text(
            text = countryToFlag(state.playerCountry.ifBlank { "US" }),
            fontSize = 26.sp,
            modifier = Modifier
                .clickable { onFlagClick() }
                .padding(end = 10.dp),
        )
        // Clan tag pill (clickable)
        Box(modifier = Modifier.clickable { onClanClick() }) {
            ClanTagPill(tag = state.clanTag, colorKey = state.clanTagColor)
        }
        Spacer(Modifier.width(8.dp))
        // Player name
        Text(
            text = state.playerName,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        // Edit icon
        IconButton(onClick = onEditNameClick) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit name",
                tint = OrangeAccent,
            )
        }
    }
}

// ── Avatar card ──────────────────────────────────────────────────────────

@Composable
private fun AvatarCard(
    state: GameStateEntity,
    language: String,
    onGenderChange: (String) -> Unit,
    onChangeAvatar: () -> Unit,
) {
    val resId = rememberAvatarResId(state.heroAvatar, state.playerGender)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(DarkSurfaceVariant, RoundedCornerShape(14.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = state.heroAvatar,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Gender toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("male" to "♂ Male", "female" to "♀ Female").forEach { (g, label) ->
                val selected = state.playerGender == g
                Button(
                    onClick = { onGenderChange(g) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) OrangeAccent else DarkSurfaceVariant,
                        contentColor = if (selected) Color.Black else TextSecondary,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Change avatar button
        OutlinedButton(
            onClick = onChangeAvatar,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text(
                text = AppStrings.t(language, "change_avatar_btn"),
                color = OrangeAccent,
            )
        }
    }
}

// ── Friends ──────────────────────────────────────────────────────────────

@Composable
private fun FriendsCard(viewModel: GameViewModel, language: String) {
    val myCode by viewModel.myFriendCode.collectAsState()
    val friends by viewModel.friendsList.collectAsState()
    val addResult by viewModel.addFriendResult.collectAsState()
    var inputCode by remember { mutableStateOf("") }
    val context = LocalContext.current
    val clipboard = remember {
        context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
    }

    // Auto-clear feedback message after 2.5s
    LaunchedEffect(addResult) {
        if (addResult != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.consumeAddFriendResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        // ── My friend code (share with others) ──────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = AppStrings.t(language, "your_code"),
                color = TextSecondary,
                fontSize = 12.sp,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = myCode ?: "------",
                color = OrangeAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(OrangeAccent.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
            Spacer(Modifier.width(8.dp))
            if (myCode != null) {
                TextButton(
                    onClick = {
                        clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("friend code", myCode))
                        android.widget.Toast.makeText(
                            context,
                            AppStrings.t(language, "copied"),
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(AppStrings.t(language, "btn_copy"), color = OrangeAccent, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Add friend by code ──────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = inputCode,
                onValueChange = {
                    inputCode = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(6)
                },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TextPrimary),
                placeholder = {
                    Text(
                        text = AppStrings.t(language, "friend_code_label"),
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = OrangeAccent,
                ),
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    viewModel.addFriendByCode(inputCode)
                    inputCode = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(AppStrings.t(language, "btn_add"), color = Color.White, fontSize = 13.sp)
            }
        }

        addResult?.let { result ->
            Spacer(Modifier.height(6.dp))
            Text(
                text = result,
                fontSize = 12.sp,
                color = if (result.startsWith("✅")) Color(0xFF34C759) else Color(0xFFFF453A),
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Friends list ────────────────────────────────────────────────
        Text(
            text = AppStrings.t(language, "friends_header"),
            fontSize = 14.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        if (friends.isEmpty()) {
            Text(
                text = AppStrings.t(language, "friends_empty"),
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else {
            // 5 visible rows × ~36dp each, scroll for more
            LazyColumn(modifier = Modifier.heightIn(max = (5 * 36).dp)) {
                items(friends, key = { it.uid }) { f ->
                    FriendRow(f, onRemove = { viewModel.removeFriend(f.uid) })
                }
            }
        }
    }
}

@Composable
private fun FriendRow(friend: LeaderboardEntry, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = countryToFlag(friend.country.ifBlank { "US" }), fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        if (friend.clanTag.isNotBlank()) {
            ClanTagText(tag = friend.clanTag, colorKey = friend.clanTagColor, fontSize = 12)
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = friend.name,
            color = TextPrimary,
            fontSize = 14.sp,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "R:${friend.prestige}",
            color = Color(0xFF3FA9F5),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.width(36.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "L${friend.level}",
            color = OrangeAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.width(36.dp),
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(28.dp),
        ) {
            Text("✕", color = TextMuted, fontSize = 14.sp)
        }
    }
}

// ── Clan Tag editor dialog ───────────────────────────────────────────────

@Composable
private fun ClanTagEditorDialog(
    currentTag: String,
    currentColor: String,
    language: String,
    onSave: (tag: String, color: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var tag by remember { mutableStateOf(currentTag) }
    var color by remember { mutableStateOf(currentColor) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "Clan Tag",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = tag,
                onValueChange = { v ->
                    tag = v.uppercase().filter { it.isLetterOrDigit() }.take(4)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("4 chars max", color = TextMuted) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = OrangeAccent,
                ),
            )
            Spacer(Modifier.height(16.dp))
            Text(AppStrings.t(language, "color_label"), color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            // Color chips
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CLAN_TAG_COLOR_MAP.keys.forEach { key ->
                    val selected = color == key
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(clanTagColor(key))
                            .border(
                                width = if (selected) 3.dp else 1.dp,
                                color = if (selected) Color.White else TextMuted,
                                shape = CircleShape,
                            )
                            .clickable { color = key },
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) { Text(AppStrings.t(language, "btn_cancel"), color = TextSecondary) }
                Button(
                    onClick = { onSave(tag, color) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                ) { Text(AppStrings.t(language, "btn_save"), color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
