package com.example.training_tracker.domain.repository

import com.example.training_tracker.data.local.dao.WorkoutDao
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek

class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao
): WorkoutRepository {
    override val workouts = workoutDao.getAllWorkouts()

  //  private val _workoutHistory = MutableStateFlow<List<Workout>>(emptyList())
  //  val workoutHistory: StateFlow<List<Workout>> = _workoutHistory.asStateFlow()

    override suspend fun addWorkout(workout: Workout) {
        workoutDao.insert(workout)
    }

    override suspend fun updateWorkout(workout: Workout) {
        workoutDao.update(workout)
    }

    override suspend fun deleteWorkout(workout: Workout) {
        workoutDao.delete(workout)
    }

    override fun getWorkoutById(id: String) : Flow<Workout?> {
        return workouts.map { list ->
            list.find { it.id == id }
        }
    }

    override fun getWorkoutsByDay(day: DayOfWeek): Flow<List<Workout>> {
        return workouts.map { list ->
            list.filter { it.dayOfWeek == day }
        }
    }

    override fun getTodayWorkout(day: DayOfWeek): Flow<Workout?> {
        return workouts.map { list ->
            list.firstOrNull { it.dayOfWeek == day }
        }
    }

//    fun saveWorkoutToHistory(workout: Workout) {
//        // We create a copy with a new ID to avoid conflicts if the user performs the same workout again
//        val historyEntry = workout.copy(
//            id = java.util.UUID.randomUUID().toString()
//        )
//        _workoutHistory.value = listOf(historyEntry) + _workoutHistory.value
//    }
}