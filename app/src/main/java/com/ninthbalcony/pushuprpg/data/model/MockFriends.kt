package com.ninthbalcony.pushuprpg.data.model

/**
 * In-memory mock friends data. Replaced with a real source once the online
 * leaderboard backend lands (see plan: Online Leaderboard Roadmap, Stage 4).
 */
data class MockFriend(
    val name: String,
    val country: String,    // ISO-2
    val clanTag: String,    // "" = no clan
    val resets: Int,
    val level: Int,
)

val MOCK_FRIENDS: List<MockFriend> = listOf(
    MockFriend("AlexK",  "US", "WAR",  0, 23),
    MockFriend("BoltZ",  "DE", "NALC", 2, 43),
    MockFriend("Skyhi",  "JP", "SKY",  1,  8),
    MockFriend("RonX",   "BR", "",     0, 12),
    MockFriend("Maya",   "FR", "PWR",  3, 51),
    MockFriend("Nico",   "IT", "",     0,  5),
    MockFriend("Ivar",   "SE", "ODIN", 1, 30),
)
