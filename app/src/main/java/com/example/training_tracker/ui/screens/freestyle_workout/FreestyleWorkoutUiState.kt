package com.example.training_tracker.ui.screens.freestyle_workout

import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.MuscleGroups

data class FreestyleWorkoutUiState(
    val workout: Workout = Workout(name = "Freestyle Workout", isOnGoing = true, startTime = System.currentTimeMillis()),
    val availableExercises: List<Exercise> = emptyList(),
    val showExercisePicker: Boolean = false,
    val navigateToReportId: String? = null,
    val pendingExerciseName: String? = null,
    val showMuscleGroupPicker: Boolean = false,
    val showSaveRoutineDialog: Boolean = false
)
