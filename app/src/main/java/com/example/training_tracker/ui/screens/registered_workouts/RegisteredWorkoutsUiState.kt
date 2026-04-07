package com.example.training_tracker.ui.screens.registered_workouts

import com.example.training_tracker.data.models.Workout
import java.time.DayOfWeek

data class RegisteredWorkoutsUiState(
    val workoutsByDay: Map<DayOfWeek, List<Workout>> = emptyMap(),
    val isLoading: Boolean = false
)