package com.ninthbalcony.pushuprpg.utils

/** Generates a random hero name like "IronFist42" or "ShadowKnight7". */
fun randomHeroName(): String {
    val prefixes = listOf(
        "Iron", "Shadow", "Dark", "Storm", "Fire",
        "Thunder", "Steel", "Frost", "War", "Night",
        "Brave", "Swift", "Stone", "Wild", "Red",
        "Silver", "Gold", "Ghost", "Rune", "Chaos"
    )
    val suffixes = listOf(
        "Fist", "Knight", "Blade", "Wolf", "Hawk",
        "Bear", "Fury", "Claw", "Strike", "Heart",
        "King", "Rage", "Skull", "Viper", "Titan",
        "Fang", "Horn", "Flame", "Storm", "Punch"
    )
    val number = (1..99).random()
    return "${prefixes.random()}${suffixes.random()}$number"
}
