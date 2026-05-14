package com.example.training_tracker.ui.screens.workout_history

import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.WorkoutHistory
import java.time.LocalDate

data class WorkoutHistoryUiState(
    val savedWorkouts: List<WorkoutHistory> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
    val selectedDate: LocalDate? = null,
    val selectedMuscleGroup: MuscleGroups? = null,
    val currentCalendarMonth: LocalDate = LocalDate.now().withDayOfMonth(1),
    val currentPage: Int = 0,
    val totalPages: Int = 1
)

enum class SortOrder {
    DATE_ASC,
    DATE_DESC,
    NAME_ASC,
    NAME_DESC
}
