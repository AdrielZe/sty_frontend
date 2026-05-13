package com.example.training_tracker.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.training_tracker.data.local.dao.ExerciseDao
import com.example.training_tracker.data.local.dao.RecordsDao
import com.example.training_tracker.data.local.dao.UserDao
import com.example.training_tracker.data.local.dao.WorkoutDao
import com.example.training_tracker.data.local.dao.WorkoutHistoryDao
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.Records
import com.example.training_tracker.data.models.User
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.WorkoutHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Workout::class, Exercise::class, WorkoutHistory::class, Records::class, User::class],
    version = 34,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutHistoryDao(): WorkoutHistoryDao
    abstract fun recordsDao() : RecordsDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_32_33 = object : Migration(32, 33) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE exercises ADD COLUMN type TEXT NOT NULL DEFAULT 'STRENGTH'")
                database.execSQL("ALTER TABLE exercises ADD COLUMN time TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE exercises ADD COLUMN distance TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_33_34 = object : Migration(33, 34) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE records ADD COLUMN cardioRecordsMap TEXT NOT NULL DEFAULT '{}'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_32_33, MIGRATION_33_34)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
