package com.example.training_tracker.ui.screens.workout_history

import com.example.training_tracker.data.models.Workout

data class WorkoutHistoryUiState(
    val savedWorkouts: List<Workout> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DATE_DESC
)

enum class SortOrder {
    DATE_ASC,
    DATE_DESC,
    NAME_ASC,
    NAME_DESC
}
