package com.example.training_tracker.data.models

import java.time.DayOfWeek

data class Workout(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val exercises: List<Exercise> = emptyList(),
    val isCompleted: Boolean = false,
    val dayOfWeek: DayOfWeek? = null
)