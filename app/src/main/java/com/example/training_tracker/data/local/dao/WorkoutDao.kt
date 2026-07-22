package com.example.training_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.training_tracker.data.models.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao{
    @Query("SELECT * FROM workouts")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE dayOfWeek = :day AND isCompleted = 0")
    suspend fun getActiveWorkoutForDay(day: Int): Workout?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: Workout)

    @Update
    suspend fun update(workout: Workout)

    @Delete
    suspend fun delete(workout: Workout)

    @Query("DELETE FROM workouts")
    suspend fun deleteAllWorkouts()

    @Upsert
    suspend fun insertOrUpdateAll(workouts: List<Workout>)

    @Query("DELETE FROM workouts")
    suspend fun deleteAll()

}