package com.example.training_tracker.data.models

data class Exercise (
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val sets: Int = 1,
    val reps: String = "",
    val weight: String = "",
    val setsCompleted: Int = 0
)