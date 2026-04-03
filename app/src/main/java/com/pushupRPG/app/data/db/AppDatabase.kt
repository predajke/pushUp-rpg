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
    version = 6,
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
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pushup_rpg_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }


    }
}