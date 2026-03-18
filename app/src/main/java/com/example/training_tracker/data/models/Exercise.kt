package com.example.training_tracker.data.models

data class Exercise (
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val repsDone: String = "",
    val reps: String
)