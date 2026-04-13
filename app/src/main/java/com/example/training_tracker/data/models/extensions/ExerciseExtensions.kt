package com.example.training_tracker.data.models.extensions

import com.example.training_tracker.data.models.Exercise

fun Exercise.isValidToComplete(): Boolean {
    return exerciseSets.all { set ->
        val weight = set.weight.toDoubleOrNull() ?: 0.0
        val reps = set.reps.toIntOrNull() ?: 0

        weight > 0 && reps > 0
    }
}