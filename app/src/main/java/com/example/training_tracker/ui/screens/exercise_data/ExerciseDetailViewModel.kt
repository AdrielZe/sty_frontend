package com.example.training_tracker.ui.screens.exercise_data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

const val EXERCISE_NAME_ARG = "exerciseName"

class ExerciseDetailViewModel(
    private val exerciseName: String,
    workoutHistoryRepository: WorkoutHistoryRepository
) : ViewModel() {

    val uiState: StateFlow<ExerciseDetailUiState> = workoutHistoryRepository.workoutHistories
        .map { histories ->
            val log = ExerciseDataAggregator.build(histories)
                .find { it.name.equals(exerciseName, ignoreCase = true) }
            ExerciseDetailUiState(isLoading = false, log = log)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExerciseDetailUiState(isLoading = true)
        )

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                val savedStateHandle = createSavedStateHandle()
                val name = savedStateHandle.get<String>(EXERCISE_NAME_ARG).orEmpty()
                ExerciseDetailViewModel(
                    exerciseName = name,
                    workoutHistoryRepository = application.container.workoutHistoryRepository
                )
            }
        }
    }
}
