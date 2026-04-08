package com.example.training_tracker.ui.screens.workout_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.repository.WorkoutHistoryRepository
import com.example.training_tracker.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class WorkoutHistoryViewModel(
    private val workoutHistoryRepository: WorkoutHistoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)

    val uiState: StateFlow<WorkoutHistoryUiState> = combine(
        workoutHistoryRepository.workoutHistories,
        _searchQuery,
        _sortOrder
    ) { history, query, sort ->
        val filteredList = history.filter {
            it.name.contains(query, ignoreCase = true)
        }

        val sortedList = when (sort) {
            SortOrder.DATE_ASC -> filteredList.sortedBy { it.completionDate }
            SortOrder.DATE_DESC -> filteredList.sortedByDescending { it.completionDate }
            SortOrder.NAME_ASC -> filteredList.sortedBy { it.name }
            SortOrder.NAME_DESC -> filteredList.sortedByDescending { it.name }
        }

        WorkoutHistoryUiState(
            savedWorkouts = sortedList,
            searchQuery = query,
            sortOrder = sort
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkoutHistoryUiState()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSortOrderChange(newSortOrder: SortOrder) {
        _sortOrder.value = newSortOrder
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                val workoutHistoryRepository = application.container.workoutHistoryRepository
                WorkoutHistoryViewModel(workoutHistoryRepository = workoutHistoryRepository)
            }
        }
    }
}
