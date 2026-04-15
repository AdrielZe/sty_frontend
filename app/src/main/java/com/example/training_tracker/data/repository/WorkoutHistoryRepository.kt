package com.example.training_tracker.data.repository

import com.example.training_tracker.data.models.WorkoutHistory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface WorkoutHistoryRepository {

    val workoutHistories: Flow<List<WorkoutHistory>>
    fun getWorkoutById(id: String): Flow<WorkoutHistory?>
    suspend fun addWorkoutHistory(workoutHistory: WorkoutHistory)
    suspend fun updateWorkoutHistory(workoutHistory: WorkoutHistory)
    suspend fun deleteWorkoutHistory(workoutHistory: WorkoutHistory)
    fun getHistoryByDate(date: LocalDate): Flow<List<WorkoutHistory>>
}