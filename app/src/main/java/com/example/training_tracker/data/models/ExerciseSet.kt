package com.example.training_tracker.data.models

data class ExerciseSet(
    val set: Int,
    val reps: String = "",
    val weight: String = "",
    val isCompleted: Boolean = false,
    val previousReps: String = "",
    val previousWeight: String = "",
    val technique: Technique = Technique.NORMAL
)