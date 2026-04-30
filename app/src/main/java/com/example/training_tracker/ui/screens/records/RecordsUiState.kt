package com.example.training_tracker.ui.screens.records

import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.Records

data class RecordsUiState(
    val records: Records? = null,
    val isLoading: Boolean = true,
    val selectedMuscleGroup: MuscleGroups? = null,
    val selectedExerciseHistory: Pair<String, List<Double>>? = null
)