package com.example.training_tracker.data.repository

import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek

class WorkoutRepository {
    // Mockando o treino de peito para todos os dias da semana conforme solicitado
    private val initialWorkouts = DayOfWeek.entries.map { day ->
        Workout(
            id = "mock_chest_${day.name}",
            name = "Treino de Peito",
            dayOfWeek = day,
            exercises = listOf(
                Exercise(name = "Supino reto"),
                Exercise(name = "Supino inclinado"),
                Exercise(name = "Voador"),
                Exercise(name = "Crucifixo")
            )
        )
    }

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

    fun getWorkoutsByDay(day: DayOfWeek): Flow<List<Workout>> {
        return _workouts.map { list ->
            list.filter { it.dayOfWeek == day }
        }
    }

    fun getTodayWorkout(day: DayOfWeek): Flow<Workout?> {
        return _workouts.map { list ->
            // Prioriza o treino mockado de peito ou o primeiro treino encontrado para o dia
            list.firstOrNull { it.dayOfWeek == day }
        }
    }
}