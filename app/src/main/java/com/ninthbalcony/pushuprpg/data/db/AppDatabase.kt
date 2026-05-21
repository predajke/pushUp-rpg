package com.ninthbalcony.pushuprpg.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 1,
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
