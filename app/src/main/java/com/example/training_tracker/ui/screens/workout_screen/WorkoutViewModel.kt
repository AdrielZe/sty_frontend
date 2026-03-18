package com.example.training_tracker.ui.screens.workout_screen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.repository.WorkoutRepository
import com.example.training_tracker.ui.screens.home.HomeViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class WorkoutViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private val workoutId: String = checkNotNull(savedStateHandle["workoutId"])


    val uiState = workoutRepository.getWorkoutById(workoutId)
        .map { workout ->
            WorkoutUiState(
                workout = workout
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkoutUiState()
        )

    fun updateExerciseReps(exerciseId: String, newReps: String) {
        val currentWorkout = uiState.value.workout ?: return

        val updatedExercise = currentWorkout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                exercise.copy(repsDone = newReps)
            } else exercise
        }

        val updatedWorkout = currentWorkout.copy(exercises = updatedExercise)

        workoutRepository.updateWorkout(updatedWorkout)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                // Pega a instância da nossa GymTrackerApplication
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                val workoutRepository = application.container.workoutRepository

                val savedStateHandle = createSavedStateHandle()

                WorkoutViewModel(savedStateHandle = savedStateHandle, workoutRepository = workoutRepository )
            }
        }
    }
}