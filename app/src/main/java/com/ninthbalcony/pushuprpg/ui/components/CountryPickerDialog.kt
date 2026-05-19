package com.ninthbalcony.pushuprpg.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ninthbalcony.pushuprpg.ui.theme.*
import com.ninthbalcony.pushuprpg.utils.AppStrings
import com.ninthbalcony.pushuprpg.utils.allCountryCodes
import com.ninthbalcony.pushuprpg.utils.countryDisplayName
import com.ninthbalcony.pushuprpg.utils.countryToFlag

@Composable
fun CountryPickerDialog(
    currentCode: String,
    language: String,
    onPick: (code: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val allCodes = remember { allCountryCodes() }
    val items = remember(query, allCodes) {
        val q = query.trim().lowercase()
        allCodes
            .map { it to countryDisplayName(it) }
            .filter { (code, name) ->
                q.isEmpty() || code.lowercase().contains(q) || name.lowercase().contains(q)
            }
            .sortedBy { it.second }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = AppStrings.t(language, "choose_country"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search…", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = OrangeAccent,
                ),
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(items, key = { it.first }) { (code, name) ->
                    val selected = code.equals(currentCode, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(code) }
                            .background(if (selected) OrangeAccent.copy(alpha = 0.15f) else Color.Transparent)
                            .padding(horizontal = 6.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(countryToFlag(code), fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(name, color = TextPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
                        Text(code, color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(AppStrings.t(language, "close"), color = TextSecondary)
            }
        }
    }
}
