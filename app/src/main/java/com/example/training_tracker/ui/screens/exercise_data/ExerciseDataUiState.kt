package com.example.training_tracker.ui.screens.exercise_data

import com.example.training_tracker.data.models.MuscleGroups

data class ExerciseDataUiState(
    val isLoading: Boolean = true,
    val exercises: List<ExerciseLog> = emptyList(),
    val totalExercises: Int = 0,
    val totalSessions: Int = 0,
    val totalVolumeLifted: Double = 0.0,
    val searchQuery: String = "",
    val selectedMuscleGroup: MuscleGroups? = null,
    val availableMuscleGroups: List<MuscleGroups> = emptyList()
)

data class ExerciseDetailUiState(
    val isLoading: Boolean = true,
    val log: ExerciseLog? = null,
    /** Null quando o exercício não foi realizado hoje. */
    val todayHighlight: TodayHighlightData? = null
)
