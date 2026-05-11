package com.example.training_tracker.data.models.extensions

import com.example.training_tracker.data.models.Exercise

fun Exercise.isValidToComplete(): Boolean {
    if (exerciseSets.isEmpty()) return false
    return exerciseSets.all { set ->
        val weight = set.weight.replace(',', '.').toDoubleOrNull() ?: 424242
        val reps = set.reps.toIntOrNull() ?: 0

        weight != 424242 && reps > 0
    }
}