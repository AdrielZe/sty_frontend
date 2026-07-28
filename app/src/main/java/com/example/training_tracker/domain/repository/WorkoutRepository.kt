package com.example.training_tracker.domain.repository

import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.WorkoutToDelete
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.util.UUID

interface WorkoutRepository{
    val workouts : Flow<List<Workout>>
    suspend fun addWorkout(workout: Workout)
    suspend fun updateWorkout(workout: Workout)
    suspend fun updateWorkoutsWithCount(workouts: List<Workout>): Int
    suspend fun deleteWorkoutById(workoutId: String)
    suspend fun deleteAllWorkouts()
    fun getWorkoutById(id: String) : Flow<Workout?>
    fun getWorkoutsByDay(day: DayOfWeek, userId: UUID): Flow<List<Workout>>
    fun getTodayWorkout(day: DayOfWeek): Flow<Workout?>
    suspend fun getAllNotSyncedWorkouts(): List<Workout>
    suspend fun getPendingWorkoutsToDelete(): List<WorkoutToDelete>
}
