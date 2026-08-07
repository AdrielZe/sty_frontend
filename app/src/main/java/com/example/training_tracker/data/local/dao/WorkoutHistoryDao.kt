package com.example.training_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.WorkoutHistory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WorkoutHistoryDao {
    @Query("SELECT * FROM workoutHistories")
    fun getAllWorkoutHistories(): Flow<List<WorkoutHistory>>

    @Query("SELECT * FROM workoutHistories WHERE completionDate = :date")
    fun getHistoryByDate(date: LocalDate): Flow<List<WorkoutHistory>>

    @Query("SELECT * FROM workoutHistories WHERE id = :id")
    fun getWorkoutById(id: String): Flow<WorkoutHistory?>

    @Upsert
    suspend fun insert(workoutHistory: WorkoutHistory) : Long


    @Upsert
    suspend fun insertOrUpdateAll(histores: List<WorkoutHistory>)

    @Update
    suspend fun updateAll(histories: List<WorkoutHistory>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(histories: List<WorkoutHistory>)

    @Update
    suspend fun update(workoutHistory: WorkoutHistory)

    @Delete
    suspend fun delete(workoutHistory: WorkoutHistory)

    @Query("DELETE FROM workoutHistories")
    suspend fun deleteAll()

    @Query("SELECT * FROM workoutHistories WHERE isCompleted = 1 ORDER BY completionDate DESC, completionTime DESC")
    fun getCompletedWorkoutHistories(): Flow<List<WorkoutHistory>>

    @Query("SELECT * FROM workoutHistories WHERE isSynced = 0")
    suspend fun getAllNotSyncedHistories(): List<WorkoutHistory>
}