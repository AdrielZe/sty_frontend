package com.example.training_tracker.ui.screens.workout_history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.WorkoutHistory
import com.example.training_tracker.data.repository.WorkoutHistoryRepository
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
    private val _selectedMuscleGroup = MutableStateFlow<MuscleGroups?>(null)
    private val _currentCalendarMonth = MutableStateFlow<LocalDate>(LocalDate.now().withDayOfMonth(1))

    val uiState: StateFlow<WorkoutHistoryUiState> = combine(
        workoutHistoryRepository.workoutHistories,
        _searchQuery,
        _sortOrder,
        _selectedDate,
        _selectedMuscleGroup,
        _currentCalendarMonth
    ) { args: Array<Any?> ->
        val history = args[0] as List<WorkoutHistory>
        val query = args[1] as String
        val sort = args[2] as SortOrder
        val selectedDate = args[3] as LocalDate?
        val selectedMuscle = args[4] as MuscleGroups?
        val currentMonth = args[5] as LocalDate

        val filteredList = history.filter { workout ->
            val matchesQuery = workout.name.contains(query, ignoreCase = true)
            val matchesDate = selectedDate == null || workout.completionDate == selectedDate
            val matchesMuscle = selectedMuscle == null || workout.exercises.any { it.muscleGroup == selectedMuscle }
            
            matchesQuery && matchesDate && matchesMuscle
        }

        val sortedList = when (sort) {
            SortOrder.DATE_ASC -> filteredList.sortedBy { it.completionDate }
            SortOrder.DATE_DESC -> filteredList.sortedByDescending { it.completionDate }
            SortOrder.NAME_ASC -> filteredList.sortedBy { it.name.lowercase() }
            SortOrder.NAME_DESC -> filteredList.sortedByDescending { it.name.lowercase() }
        }

        WorkoutHistoryUiState(
            savedWorkouts = sortedList,
            searchQuery = query,
            sortOrder = sort,
            selectedDate = selectedDate,
            selectedMuscleGroup = selectedMuscle,
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

    fun onMuscleGroupSelected(muscleGroup: MuscleGroups?) {
        _selectedMuscleGroup.value = if (_selectedMuscleGroup.value == muscleGroup) null else muscleGroup
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
