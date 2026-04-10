package com.example.training_tracker.ui.screens.workout_screen

import com.example.training_tracker.data.models.Workout

data class WorkoutUiState(
    val workout: Workout ?= null,
    val workoutProgress: Float = 0.0f,
)