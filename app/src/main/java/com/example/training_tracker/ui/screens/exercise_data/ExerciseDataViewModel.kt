package com.example.training_tracker.ui.screens.exercise_data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ExerciseDataViewModel(
    workoutHistoryRepository: WorkoutHistoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedMuscleGroup = MutableStateFlow<MuscleGroups?>(null)

    val uiState: StateFlow<ExerciseDataUiState> = combine(
        workoutHistoryRepository.workoutHistories,
        _searchQuery,
        _selectedMuscleGroup
    ) { histories, query, group ->
        val allLogs = ExerciseDataAggregator.build(histories)

        val trimmedQuery = query.trim()
        val filtered = allLogs
            .filter { group == null || it.muscleGroup == group }
            .filter { trimmedQuery.isEmpty() || it.name.contains(trimmedQuery, ignoreCase = true) }
            .sortedByDescending { it.lastSession?.date }

        ExerciseDataUiState(
            isLoading = false,
            exercises = filtered,
            totalExercises = allLogs.size,
            totalSessions = allLogs.sumOf { it.sessionCount },
            totalVolumeLifted = allLogs.sumOf { it.totalVolumeLifted },
            searchQuery = query,
            selectedMuscleGroup = group,
            availableMuscleGroups = MuscleGroups.entries.filter { entry ->
                allLogs.any { it.muscleGroup == entry }
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExerciseDataUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onMuscleGroupSelected(muscleGroup: MuscleGroups?) {
        _selectedMuscleGroup.value = muscleGroup
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                ExerciseDataViewModel(application.container.workoutHistoryRepository)
            }
        }
    }
}
