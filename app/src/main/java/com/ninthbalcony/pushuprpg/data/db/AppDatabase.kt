package com.ninthbalcony.pushuprpg.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
                    // ⚠ DEV ONLY — удалить перед публикацией в Play Console.
                    // Сейчас стирает БД при любом mismatch версии (напр. v21 на старом
                    // устройстве при выкатывании v1). После релиза → заменить
                    // на цепочку Migration(1, 2), Migration(2, 3) и т.д.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { INSTANCE = it }
            }
        }

        /** Только для тестов — подменяет singleton in-memory базой. */
        fun setTestInstance(db: AppDatabase) { INSTANCE = db }
        fun clearTestInstance() { INSTANCE = null }
    }
}
