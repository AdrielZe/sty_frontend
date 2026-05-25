package com.example.training_tracker.fakes

import com.example.training_tracker.data.models.WorkoutHistory
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FakeWorkoutHistoryRepository(initial: List<WorkoutHistory> = emptyList()) : WorkoutHistoryRepository {
    private val _histories = MutableStateFlow(initial)
    override val workoutHistories: Flow<List<WorkoutHistory>> = _histories

    override fun getWorkoutById(id: String): Flow<WorkoutHistory?> =
        _histories.map { list -> list.firstOrNull { it.id == id } }
    override suspend fun addWorkoutHistory(workoutHistory: WorkoutHistory) {
        _histories.value = _histories.value + workoutHistory
    }
    override suspend fun updateWorkoutHistory(workoutHistory: WorkoutHistory) {
        _histories.value = _histories.value.map { if (it.id == workoutHistory.id) workoutHistory else it }
    }
    override suspend fun deleteWorkoutHistory(workoutHistory: WorkoutHistory) {
        _histories.value = _histories.value.filterNot { it.id == workoutHistory.id }
    }
    override fun getHistoryByDate(date: LocalDate): Flow<List<WorkoutHistory>> =
        _histories.map { list -> list.filter { it.completionDate == date } }
}
