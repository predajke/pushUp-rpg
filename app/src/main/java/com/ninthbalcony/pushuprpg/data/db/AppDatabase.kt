package com.ninthbalcony.pushuprpg.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ninthbalcony.pushuprpg.BuildConfig
import com.ninthbalcony.pushuprpg.data.db.dao.MaxPushUpsDao
import com.ninthbalcony.pushuprpg.data.db.entity.MaxPushUpsAttemptEntity

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE game_state ADD COLUMN nightSpinWins INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE game_state ADD COLUMN nightSpinNothing INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE game_state ADD COLUMN nightEnchantMaxLevel INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE game_state ADD COLUMN totalPunchesAllTime INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [
        GameStateEntity::class,
        PushUpRecordEntity::class,
        LogEntryEntity::class,
        MaxPushUpsAttemptEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pushUpDao(): PushUpDao
    abstract fun maxPushUpsDao(): MaxPushUpsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pushup_rpg_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .apply {
                        if (BuildConfig.DEBUG) {
                            fallbackToDestructiveMigration(dropAllTables = true)
                        }
                    }
                    .build().also { INSTANCE = it }
            }
        }

        /** Только для тестов — подменяет singleton in-memory базой. */
        fun setTestInstance(db: AppDatabase) { INSTANCE = db }
        fun clearTestInstance() { INSTANCE = null }
    }
}
