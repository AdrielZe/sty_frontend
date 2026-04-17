package com.example.training_tracker.ui.screens.workout_history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
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
import java.time.LocalDate

class WorkoutHistoryViewModel(
    private val workoutHistoryRepository: WorkoutHistoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _currentCalendarMonth = MutableStateFlow<LocalDate>(LocalDate.now().withDayOfMonth(1))

    val uiState: StateFlow<WorkoutHistoryUiState> = combine(
        workoutHistoryRepository.workoutHistories,
        _searchQuery,
        _sortOrder,
        _selectedDate,
        _currentCalendarMonth
    ) { history, query, sort, selectedDate, currentMonth ->
        val filteredList = history.filter {
            it.name.contains(query, ignoreCase = true) &&
                    (selectedDate == null || it.completionDate == selectedDate)
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
            sortOrder = sort,
            selectedDate = selectedDate,
            currentCalendarMonth = currentMonth
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

    fun onDateSelected(date: LocalDate?) {
        _selectedDate.value = if (_selectedDate.value == date) null else date
    }

    fun onMoveMonth(delta: Long) {
        _currentCalendarMonth.update { it.plusMonths(delta) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                val workoutHistoryRepository = application.container.workoutHistoryRepository
                WorkoutHistoryViewModel(
                    workoutHistoryRepository = workoutHistoryRepository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
