package com.example.training_tracker.ui.screens.home

import com.example.training_tracker.data.models.User
import com.example.training_tracker.data.models.Workout

data class HomeUiState(
    val user: User? = null,
    val currentDate: String? = null,
    val todayWorkouts: List<Workout> = emptyList(),
    val totalWorkoutsCompleted: Int = 0,
    val workoutsCompletedThisWeek: Int = 0
)