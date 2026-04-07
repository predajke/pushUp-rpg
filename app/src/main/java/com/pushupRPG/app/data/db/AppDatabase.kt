package com.pushupRPG.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        GameStateEntity::class,
        PushUpRecordEntity::class,
        LogEntryEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pushUpDao(): PushUpDao

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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pushup_rpg_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
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