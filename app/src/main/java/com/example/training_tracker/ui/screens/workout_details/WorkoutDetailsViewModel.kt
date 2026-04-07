package com.example.training_tracker.ui.screens.workout_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutDetailsViewModel(
    private val workoutRepository: WorkoutRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workoutId: String = checkNotNull(savedStateHandle["workoutId"])
    private val canStart: Boolean = savedStateHandle.get<String>("canStart")?.toBoolean() ?: true

    val uiState: StateFlow<WorkoutDetailsUiState> = workoutRepository.getWorkoutById(workoutId)
        .map { workout ->
            WorkoutDetailsUiState(
                workout = workout,
                canStartWorkout = canStart,
                isEditMode = !canStart
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500),
            initialValue = WorkoutDetailsUiState(isLoading = true)
        )

    fun addExercise(name: String) {
        val currentWorkout = uiState.value.workout ?: return
        val newExercise = Exercise(name = name)
        val updatedWorkout = currentWorkout.copy(
            exercises = currentWorkout.exercises + newExercise
        )
        viewModelScope.launch {
            workoutRepository.updateWorkout(updatedWorkout)
        }
    }

    fun removeExercise(exerciseId: String) {
        val currentWorkout = uiState.value.workout ?: return
        val updatedWorkout = currentWorkout.copy(
            exercises = currentWorkout.exercises.filter { it.id != exerciseId }
        )

        viewModelScope.launch {
            workoutRepository.updateWorkout(updatedWorkout)
        }
    }

    fun moveExercise(fromIndex: Int, toIndex: Int) {
        val currentWorkout = uiState.value.workout ?: return
        val exercises = currentWorkout.exercises.toMutableList()
        if (fromIndex !in exercises.indices || toIndex !in exercises.indices) return
        
        val item = exercises.removeAt(fromIndex)
        exercises.add(toIndex, item)
        
        val updatedWorkout = currentWorkout.copy(exercises = exercises)
        viewModelScope.launch {
            workoutRepository.updateWorkout(updatedWorkout)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GymTrackerApplication)
                val workoutRepository = application.container.workoutRepository
                WorkoutDetailsViewModel(
                    workoutRepository = workoutRepository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
