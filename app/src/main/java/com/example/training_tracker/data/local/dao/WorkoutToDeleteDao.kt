package com.example.training_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.training_tracker.data.models.WorkoutToDelete

@Dao
interface WorkoutToDeleteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pendingDelete: WorkoutToDelete)

    @Query("SELECT * FROM workouts_to_delete")
    suspend fun getAllPendingDeletes(): List<WorkoutToDelete>

    @Query("DELETE FROM workouts_to_delete WHERE workoutId = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM workouts_to_delete")
    suspend fun deleteAll();
}