package com.ninthbalcony.pushuprpg.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ninthbalcony.pushuprpg.data.db.entity.MaxPushUpsAttemptEntity

@Database(
    entities = [
        GameStateEntity::class,
        PushUpRecordEntity::class,
        LogEntryEntity::class,
        MaxPushUpsAttemptEntity::class
    ],
    version = 20,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pushUpDao(): PushUpDao
    abstract fun maxPushUpsDao(): com.ninthbalcony.pushuprpg.data.db.dao.MaxPushUpsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE game_state ADD COLUMN heroAvatar TEXT NOT NULL DEFAULT 'hero_1'"
                )
            }
        }
        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_state ADD COLUMN activeEventId INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN eventStartTime INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN eventEndTime INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN lastEventTime INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE game_state ADD COLUMN teeth INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_state ADD COLUMN shopItems TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE game_state ADD COLUMN shopLastRefresh INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN forgeSlot1 TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE game_state ADD COLUMN forgeSlot2 TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE game_state ADD COLUMN cloverBoxUsedToday INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN freePointsUsedToday INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN lastDailyReset TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE game_state ADD COLUMN characterBirthDate TEXT NOT NULL DEFAULT ''"
                )
            }
        }
        private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_state ADD COLUMN totalEnchantmentsSuccess INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN totalItemsMerged INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN totalTeethSpent INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN bestSingleSession INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN totalCriticalHits INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN totalTeethEarned INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN highestMonsterLevelKilled INTEGER NOT NULL DEFAULT 0")
            }
        }
        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_state ADD COLUMN isCurrentBoss INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN currentBossId INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN lastDailyRewardDate TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE game_state ADD COLUMN dailyRewardDay INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE game_state ADD COLUMN activeQuestsJson TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE game_state ADD COLUMN lastQuestRefreshDate TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_state ADD COLUMN achievementsJson TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE game_state ADD COLUMN activeAchievementIds TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE game_state ADD COLUMN bestiaryJson TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE game_state ADD COLUMN itemLogJson TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_state ADD COLUMN bossKillsJson TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Onboarding & Analytics
                database.execSQL("ALTER TABLE game_state ADD COLUMN isFirstLaunch INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE game_state ADD COLUMN installDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                // Anti-Cheat
                database.execSQL("ALTER TABLE game_state ADD COLUMN lastSaveTime INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                // Firebase Sync
                database.execSQL("ALTER TABLE game_state ADD COLUMN lastSyncTime INTEGER NOT NULL DEFAULT 0")
                // Rate Us
                database.execSQL("ALTER TABLE game_state ADD COLUMN rateUsLastShowDate INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN rateUsDoNotShowAgain INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_12_13 = object : androidx.room.migration.Migration(12, 13) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_state ADD COLUMN shopRerollCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN shopRerollResetTime INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN lastAdQuestRerollDate TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_13_14 = object : androidx.room.migration.Migration(13, 14) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_state ADD COLUMN adShopViewCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN adShopLastViewTime INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_14_15 = object : androidx.room.migration.Migration(14, 15) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_state ADD COLUMN prestigeLevel INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_15_16 = object : androidx.room.migration.Migration(15, 16) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_state ADD COLUMN dailySpinUsedToday INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN dailySpinAdViewsToday INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN lastDailySpinReset TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE game_state ADD COLUMN totalShopPurchases INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN totalEnchantAttempts INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN totalMergeAttempts INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_16_17 = object : androidx.room.migration.Migration(16, 17) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_state ADD COLUMN spinTokens INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_17_18 = object : androidx.room.migration.Migration(17, 18) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_state ADD COLUMN teethFromQuests INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN teethFromAds INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN teethFromSpin INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN itemsFromSpin INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_18_19 = object : androidx.room.migration.Migration(18, 19) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_state ADD COLUMN lastHourlySpinGrantTime INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_19_20 = object : androidx.room.migration.Migration(19, 20) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_state ADD COLUMN punchesUsedToday INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_state ADD COLUMN lastPunchDate TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Max pushups (99) tracking for anti-cheat
                database.execSQL("""
                    CREATE TABLE max_pushups_attempts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        date TEXT NOT NULL,
                        timestamp INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                        attemptNumber INTEGER NOT NULL
                    )
                """)
                database.execSQL("CREATE INDEX idx_date ON max_pushups_attempts(date)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pushup_rpg_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /** Только для тестов — подменяет singleton in-memory базой. */
        fun setTestInstance(db: AppDatabase) { INSTANCE = db }
        fun clearTestInstance() { INSTANCE = null }
    }
}