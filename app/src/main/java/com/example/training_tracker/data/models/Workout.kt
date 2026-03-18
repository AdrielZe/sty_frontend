package com.example.training_tracker.data.models

data class Workout(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val exercises: List<Exercise> = emptyList()
)