package com.example.training_tracker.domain.repository

import com.example.training_tracker.data.models.Workout
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

interface WorkoutRepository{
    val workouts : Flow<List<Workout>>
    suspend fun addWorkout(workout: Workout)
    suspend fun updateWorkout(workout: Workout)
    suspend fun deleteWorkout(workout: Workout)
    fun getWorkoutById(id: String) : Flow<Workout?>
    fun getWorkoutsByDay(day: DayOfWeek): Flow<List<Workout>>
    fun getTodayWorkout(day: DayOfWeek): Flow<Workout?>
}
