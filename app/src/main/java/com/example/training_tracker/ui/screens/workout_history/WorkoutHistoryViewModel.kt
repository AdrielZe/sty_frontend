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
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.WorkoutHistory
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import com.example.training_tracker.domain.repository.WorkoutRepository
import com.example.training_tracker.session_manager.SessionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

class WorkoutHistoryViewModel(
    private val workoutHistoryRepository: WorkoutHistoryRepository,
    private val workoutRepository: WorkoutRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _selectedMuscleGroup = MutableStateFlow<MuscleGroups?>(null)
    private val _currentCalendarMonth = MutableStateFlow<LocalDate>(LocalDate.now().withDayOfMonth(1))
    private val _currentPage = MutableStateFlow(0)

    val uiState: StateFlow<WorkoutHistoryUiState> = combine(
        workoutHistoryRepository.workoutHistories,
        _searchQuery,
        _sortOrder,
        _selectedDate,
        _selectedMuscleGroup,
        _currentCalendarMonth,
        _currentPage
    ) { args: Array<Any?> ->
        val history = args[0] as List<WorkoutHistory>
        val query = args[1] as String
        val sort = args[2] as SortOrder
        val selectedDate = args[3] as LocalDate?
        val selectedMuscle = args[4] as MuscleGroups?
        val currentMonth = args[5] as LocalDate
        val page = args[6] as Int

        val filteredList = history.filter { workout ->
            val matchesQuery = workout.name.contains(query, ignoreCase = true)
            val matchesDate = selectedDate == null || workout.completionDate == selectedDate
            val matchesMuscle = selectedMuscle == null || workout.exercises.any { it.muscleGroup == selectedMuscle }
            matchesQuery && matchesDate && matchesMuscle
        }

        val sortedList = when (sort) {
            SortOrder.DATE_ASC -> filteredList.sortedWith(
                compareBy<WorkoutHistory> { it.completionDate }.thenBy { it.completionTime }
            )
            SortOrder.DATE_DESC -> filteredList.sortedWith(
                compareByDescending<WorkoutHistory> { it.completionDate }.thenByDescending { it.completionTime }
            )
            SortOrder.NAME_ASC -> filteredList.sortedBy { it.name.lowercase() }
            SortOrder.NAME_DESC -> filteredList.sortedByDescending { it.name.lowercase() }
        }

        val totalPages = maxOf(1, (sortedList.size + PAGE_SIZE - 1) / PAGE_SIZE)
        val safePage = page.coerceIn(0, totalPages - 1)
        val pagedList = sortedList.drop(safePage * PAGE_SIZE).take(PAGE_SIZE)

        WorkoutHistoryUiState(
            savedWorkouts = pagedList,
            searchQuery = query,
            sortOrder = sort,
            selectedDate = selectedDate,
            selectedMuscleGroup = selectedMuscle,
            currentCalendarMonth = currentMonth,
            currentPage = safePage,
            totalPages = totalPages
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkoutHistoryUiState()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        _currentPage.value = 0
    }

    fun onSortOrderChange(newSortOrder: SortOrder) {
        _sortOrder.value = newSortOrder
        _currentPage.value = 0
    }

    fun onDateSelected(date: LocalDate?) {
        _selectedDate.value = if (_selectedDate.value == date) null else date
        _currentPage.value = 0
    }

    fun onMuscleGroupSelected(muscleGroup: MuscleGroups?) {
        _selectedMuscleGroup.value = if (_selectedMuscleGroup.value == muscleGroup) null else muscleGroup
        _currentPage.value = 0
    }

    fun onMoveMonth(delta: Long) {
        _currentCalendarMonth.update { it.plusMonths(delta) }
    }

    fun onPageChange(page: Int) {
        _currentPage.value = page
    }

  fun duplicateFromHistory(history: WorkoutHistory, targetDay: DayOfWeek) {

      viewModelScope.launch {
          val newWorkout = Workout(
                id = java.util.UUID.randomUUID().toString(),
                userId = sessionManager.userIdFlow.first().toString(),
                name = history.name,
                exercises = history.exercises,
                dayOfWeek = targetDay
            )

            try {
                workoutRepository.addWorkout(newWorkout)
            } catch (e: Exception) {
                _uiEvent.send("Erro ao duplicar: ${e.message}")
            }
        }
    }

    companion object {
        const val PAGE_SIZE = 10

        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                val workoutHistoryRepository = application.container.workoutHistoryRepository
                val workoutRepository = application.container.workoutRepository
                val sessionManager = application.container.sessionManager
                WorkoutHistoryViewModel(
                    workoutHistoryRepository = workoutHistoryRepository,
                    workoutRepository = workoutRepository,
                    savedStateHandle = createSavedStateHandle(),
                    sessionManager = sessionManager
                )
            }
        }
    }
}
