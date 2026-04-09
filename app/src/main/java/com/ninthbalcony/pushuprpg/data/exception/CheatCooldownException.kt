package com.ninthbalcony.pushuprpg.data.exception

import com.ninthbalcony.pushuprpg.managers.AdType

class CheatCooldownException(
    val remainingMs: Long,
    val adType: AdType,
    val attemptNumber: Int
) : Exception("Anti-cheat cooldown active: $remainingMs ms remaining (attempt #$attemptNumber, ad: $adType)")
