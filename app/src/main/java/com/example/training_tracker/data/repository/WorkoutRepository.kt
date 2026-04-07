package com.example.training_tracker.data.repository

import com.example.training_tracker.data.local.dao.WorkoutDao
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.mocks.initialWorkouts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDateTime

interface WorkoutRepository{
    val workouts : Flow<List<Workout>>

    //val workoutHistory: StateFlow<List<Workout>> = _workoutHistory.asStateFlow()
    suspend fun addWorkout(workout: Workout)
    suspend fun updateWorkout(workout: Workout)
    suspend fun deleteWorkout(workout: Workout)
    fun getWorkoutById(id: String) : Flow<Workout?>
    fun getWorkoutsByDay(day: DayOfWeek): Flow<List<Workout>>
    fun getTodayWorkout(day: DayOfWeek): Flow<Workout?>

//    fun saveWorkoutToHistory(workout: Workout) {
//        // We create a copy with a new ID to avoid conflicts if the user performs the same workout again
//        val historyEntry = workout.copy(
//            id = java.util.UUID.randomUUID().toString()
//        )
//        _workoutHistory.value = listOf(historyEntry) + _workoutHistory.value
//    }
}
