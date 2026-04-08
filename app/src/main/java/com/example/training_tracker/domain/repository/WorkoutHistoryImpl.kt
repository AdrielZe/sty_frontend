package com.example.training_tracker.domain.repository

import com.example.training_tracker.data.local.dao.WorkoutHistoryDao
import com.example.training_tracker.data.models.WorkoutHistory
import com.example.training_tracker.data.repository.WorkoutHistoryRepository

class WorkoutHistoryImpl(
    private val workoutHistoryDao: WorkoutHistoryDao
) : WorkoutHistoryRepository{
    override val workoutHistories = workoutHistoryDao.getAllWorkoutHistories()

    override suspend fun addWorkoutHistory(workoutHistory: WorkoutHistory) {
        workoutHistoryDao.insert(workoutHistory)
    }

    override suspend fun deleteWorkoutHistory(workoutHistory: WorkoutHistory) {
        workoutHistoryDao.delete(workoutHistory)
    }

    override suspend fun updateWorkoutHistory(workoutHistory: WorkoutHistory) {
        workoutHistoryDao.update(workoutHistory)
    }
}
