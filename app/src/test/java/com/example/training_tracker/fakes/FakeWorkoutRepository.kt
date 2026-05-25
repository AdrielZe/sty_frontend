package com.example.training_tracker.fakes

import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek

class FakeWorkoutRepository(initial: List<Workout> = emptyList()) : WorkoutRepository {
    private val _workouts = MutableStateFlow(initial)
    override val workouts: Flow<List<Workout>> = _workouts

    var addError: Throwable? = null
    val added = mutableListOf<Workout>()
    val updated = mutableListOf<Workout>()
    val deleted = mutableListOf<Workout>()

    override suspend fun addWorkout(workout: Workout) {
        addError?.let { throw it }
        added += workout
        _workouts.value = _workouts.value + workout
    }
    override suspend fun updateWorkout(workout: Workout) {
        updated += workout
        _workouts.value = _workouts.value.map { if (it.id == workout.id) workout else it }
    }
    override suspend fun deleteWorkout(workout: Workout) {
        deleted += workout
        _workouts.value = _workouts.value.filterNot { it.id == workout.id }
    }
    override fun getWorkoutById(id: String): Flow<Workout?> =
        _workouts.map { list -> list.firstOrNull { it.id == id } }
    override fun getWorkoutsByDay(day: DayOfWeek): Flow<List<Workout>> =
        _workouts.map { list -> list.filter { it.dayOfWeek == day } }
    override fun getTodayWorkout(day: DayOfWeek): Flow<Workout?> =
        _workouts.map { list -> list.firstOrNull { it.dayOfWeek == day } }
}
