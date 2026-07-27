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
import java.util.UUID

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

    @Update
    suspend fun updateWorkoutsWithCount(workouts: List<Workout>) : Int

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM workouts")
    suspend fun deleteAllWorkouts()

    @Upsert
    suspend fun insertOrUpdateAll(workouts: List<Workout>)

    @Query("DELETE FROM workouts")
    suspend fun deleteAll()

    @Query("SELECT * FROM workouts WHERE isSynced = 0")
    suspend fun getAllNotSyncedWorkouts(): List<Workout>

}