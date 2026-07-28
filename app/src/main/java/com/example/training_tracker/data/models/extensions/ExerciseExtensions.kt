package com.example.training_tracker.data.models.extensions

import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseType

fun Exercise.isValidToComplete(): Boolean {
    if (exerciseSets.isEmpty()) return false
    return when (type) {
        ExerciseType.CARDIO -> exerciseSets.all { set ->
            val distance = (set.distance ?: "").replace(',', '.').toDoubleOrNull()
            val hasTime = (set.time ?: "").contains(":")
            distance != null && hasTime
        }
        ExerciseType.STRETCHING -> exerciseSets.all { set ->
            (set.time ?: "").isNotBlank()
        }
        else -> exerciseSets.all { set ->
            val weight = set.weight.replace(',', '.').toDoubleOrNull() ?: 424242
            val reps = set.reps.toIntOrNull() ?: 0
            weight != 424242 && reps > 0
        }
    }
}