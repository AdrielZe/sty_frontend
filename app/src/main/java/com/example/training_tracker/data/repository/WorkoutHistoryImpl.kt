package com.example.training_tracker.data.repository

import com.example.training_tracker.data.local.dao.WorkoutHistoryDao
import com.example.training_tracker.data.models.WorkoutHistory
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class WorkoutHistoryImpl(
    private val workoutHistoryDao: WorkoutHistoryDao
) : WorkoutHistoryRepository {
    override val workoutHistories = workoutHistoryDao.getAllWorkoutHistories()

    override fun getWorkoutById(id: String): Flow<WorkoutHistory?> {
        return workoutHistoryDao.getWorkoutById(id)
    }

    override fun getHistoryByDate(date: LocalDate): Flow<List<WorkoutHistory>> {
        return workoutHistoryDao.getHistoryByDate(date)
    }

    override suspend fun addWorkoutHistory(workoutHistory: WorkoutHistory) {
        workoutHistoryDao.insert(workoutHistory)
    }

    override suspend fun deleteWorkoutHistory(workoutHistory: WorkoutHistory) {
        workoutHistoryDao.delete(workoutHistory)
    }

    override suspend fun updateWorkoutHistory(workoutHistory: WorkoutHistory) {
        workoutHistoryDao.update(workoutHistory)
    }

    override suspend fun deleteAll() {
        workoutHistoryDao.deleteAll()
    }
}
