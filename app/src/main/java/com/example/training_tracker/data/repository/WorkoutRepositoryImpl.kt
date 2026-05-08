package com.example.training_tracker.data.repository

import com.example.training_tracker.data.local.dao.WorkoutDao
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek

class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao
): WorkoutRepository {
    override val workouts = workoutDao.getAllWorkouts()

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
}