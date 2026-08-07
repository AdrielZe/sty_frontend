package com.example.training_tracker.data.models

import java.util.UUID

data class ExerciseSet(
    val set: Int,
    val reps: String = "",
    val weight: String = "",
    val isCompleted: Boolean = false,
    val previousReps: String = "",
    val previousWeight: String = "",
    val time: String ?= "",
    val distance: String ?= "",
    val previousTime: String ?= "",
    val previousDistance: String ?= "",
    val technique: Technique = Technique.NORMAL,
    val targetReps: String = "",
    val exerciseId: UUID,
    val targetWeight: String = ""
)