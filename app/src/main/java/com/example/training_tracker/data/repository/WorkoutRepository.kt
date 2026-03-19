package com.example.training_tracker.data.repository

import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class WorkoutRepository {
    private val initialWorkouts = listOf(
        Workout(id = "1", name = "Treino de Peito", exercises = listOf<Exercise>(
            Exercise(name = "Supino reto",),
            Exercise(name = "Supino inclinado"),
            Exercise(name = "Voador"),
            Exercise(name = "Crucifixo")
        )),
        Workout(id = "2", name = "Treino de Costas"),
        Workout(id = "3", name = "Perna Completo")
    )

    private val _workouts = MutableStateFlow<List<Workout>>(initialWorkouts)

    val workouts : StateFlow<List<Workout>> = _workouts.asStateFlow()

    fun addWorkout(workout: Workout) {
        _workouts.value = _workouts.value + workout
    }

    fun updateWorkout(updatedWorkout: Workout) {
        _workouts.value = _workouts.value.map { workout ->
            if(workout.id == updatedWorkout.id){
                updatedWorkout
            } else {
                workout
            }
        }
    }

    fun getWorkoutById(id: String) : Flow<Workout?> {
        return _workouts.map { list ->
            list.find { it.id == id }
        }
    }

    fun getTodayWorkout(): Flow<Workout?> {
        return _workouts.map { list ->
            list.firstOrNull()
        }
    }


}