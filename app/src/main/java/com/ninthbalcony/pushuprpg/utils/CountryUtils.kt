package com.ninthbalcony.pushuprpg.utils

import java.util.Locale

/** ISO-2 country code → emoji flag (e.g. "US" → 🇺🇸). */
fun countryToFlag(code: String): String {
    if (code.length != 2) return "🏳"
    val offset = 0x1F1E6 - 'A'.code
    return String(
        intArrayOf(code[0].uppercaseChar().code + offset, code[1].uppercaseChar().code + offset),
        0,
        2
    )
}

/** All ISO-2 country codes the platform knows about. ~250 entries. */
fun allCountryCodes(): List<String> = Locale.getISOCountries().toList()

/** Localized display name for a country code. Falls back to the code itself. */
fun countryDisplayName(code: String, displayLocale: Locale = Locale.getDefault()): String {
    return runCatching { Locale("", code).getDisplayCountry(displayLocale) }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: code
}

/** Auto-detect the device country code (ISO-2). Falls back to "US" if the system value is invalid. */
fun detectDeviceCountry(): String {
    val cc = Locale.getDefault().country.uppercase()
    return if (cc.length == 2 && cc.all { it.isLetter() }) cc else "US"
}
