package com.example.training_tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.training_tracker.data.local.dao.ExerciseDao
import com.example.training_tracker.data.local.dao.WorkoutDao
import com.example.training_tracker.data.local.dao.WorkoutHistoryDao
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.WorkoutHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Workout::class, Exercise::class, WorkoutHistory::class], version = 10)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao

    abstract fun workoutHistoryDao(): WorkoutHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }

        }
    }
    }

