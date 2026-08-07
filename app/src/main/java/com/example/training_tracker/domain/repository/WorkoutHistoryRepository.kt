package com.example.training_tracker.domain.repository

import com.example.training_tracker.data.models.WorkoutHistory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID

interface WorkoutHistoryRepository {
    val workoutHistories: Flow<List<WorkoutHistory>>
    fun getWorkoutById(id: String): Flow<WorkoutHistory?>
    suspend fun addWorkoutHistory(workoutHistory: WorkoutHistory)
    suspend fun updateWorkoutHistory(workoutHistory: WorkoutHistory)
    suspend fun updateAll(history: List<WorkoutHistory>)
    suspend fun deleteWorkoutHistory(workoutHistory: WorkoutHistory)
    suspend fun deleteAll()
    suspend fun insert(workoutHistory: WorkoutHistory) : Long
    fun getHistories(userId: String): Flow<List<WorkoutHistory>>
    suspend fun  getAllNotSyncedHistories(): List<WorkoutHistory>
}