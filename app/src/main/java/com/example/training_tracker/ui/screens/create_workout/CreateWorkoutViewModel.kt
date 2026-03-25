package com.example.training_tracker.ui.screens.create_workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek

class CreateWorkoutViewModel(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateWorkoutUiState())
    val uiState: StateFlow<CreateWorkoutUiState> = _uiState.asStateFlow()

    fun updateWorkoutName(name: String) {
        _uiState.update { 
            it.copy(workoutName = name) 
        }
    }

    fun updateSelectedDay(day: DayOfWeek) {
        _uiState.update { 
            it.copy(selectedDay = day) 
        }
    }

    fun addExercise(exerciseName: String) {
        if (exerciseName.isBlank()) return
        val newExercise = Exercise(name = exerciseName)
        _uiState.update { 
            it.copy(exercises = it.exercises + newExercise) 
        }
    }

    fun removeExercise(exercise: Exercise) {
        _uiState.update { 
            it.copy(exercises = it.exercises - exercise) 
        }
    }

    fun saveWorkout() {
        if (_uiState.value.canSave) {
            val currentState = _uiState.value
            val newWorkout = Workout(
                name = currentState.workoutName,
                exercises = currentState.exercises,
                dayOfWeek = currentState.selectedDay
            )
            workoutRepository.addWorkout(newWorkout)
            _uiState.update { it.copy(isWorkoutSaved = true) }
        } else {
            _uiState.update { it.copy(showErrors = true) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GymTrackerApplication)
                val workoutRepository = application.container.workoutRepository
                CreateWorkoutViewModel(workoutRepository = workoutRepository)
            }
        }
    }
}