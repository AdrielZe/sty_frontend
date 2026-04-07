package com.example.training_tracker.ui.screens.workout_details

import com.example.training_tracker.data.models.Workout

data class WorkoutDetailsUiState(
    val workout: Workout? = null,
    val isLoading: Boolean = false,
    val canStartWorkout: Boolean = true,
    val isEditMode: Boolean = false
)
