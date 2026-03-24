package com.example.training_tracker.data.models

data class Exercise (
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val exerciseSets: List<ExerciseSet> = listOf(ExerciseSet(1, "", "")),
    val isCompleted: Boolean = false,
    val setsCompleted: Int = 1
)