package com.example.training_tracker.data.models

import androidx.compose.ui.text.font.FontWeight

data class ExerciseSet(
    val set: Int,
    val reps: String = "",
    val weight: String = "",
    val isCompleted: Boolean = false
)