package com.example.training_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.training_tracker.data.models.WorkoutHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutHistoryDao {
    @Query("SELECT * FROM workoutHistories")
    fun getAllWorkoutHistories(): Flow<List<WorkoutHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workoutHistory: WorkoutHistory)

    @Update
    suspend fun update(workoutHistory: WorkoutHistory)

    @Delete
    suspend fun delete(workoutHistory: WorkoutHistory)

    @Query("DELETE FROM workoutHistories")
    suspend fun deleteAll()
}