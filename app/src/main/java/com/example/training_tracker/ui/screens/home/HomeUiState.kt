package com.example.training_tracker.ui.screens.home

import com.example.training_tracker.data.models.User
import com.example.training_tracker.data.models.Workout

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String? = null) : HomeUiState
    data class Success(
        val user: User? = null,
        val currentDate: String? = null,
        val todayWorkouts: List<Workout> = emptyList(),
        val totalWorkoutsCompleted: Int = 0,
        val workoutsCompletedThisWeek: Int = 0
    ) : HomeUiState
}
