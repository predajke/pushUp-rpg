package com.pushupRPG.app.data.exception

import com.pushupRPG.app.managers.AdType

class CheatCooldownException(
    val remainingMs: Long,
    val adType: AdType,
    val attemptNumber: Int
) : Exception("Anti-cheat cooldown active: $remainingMs ms remaining (attempt #$attemptNumber, ad: $adType)")
