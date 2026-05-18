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

@Database(
    entities = [
        GameStateEntity::class,
        PushUpRecordEntity::class,
        LogEntryEntity::class,
        MaxPushUpsAttemptEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pushUpDao(): PushUpDao
    abstract fun maxPushUpsDao(): MaxPushUpsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** v2 → v3: добавлен GameStateEntity.lastUpdated для облачной синхронизации. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE game_state ADD COLUMN lastUpdated INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** v3 → v4: добавлен GameStateEntity.lastStreakRewardClaimedDay для streak rewards. */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE game_state ADD COLUMN lastStreakRewardClaimedDay INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pushup_rpg_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
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
