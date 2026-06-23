package com.example.training_tracker.data.remote.exercise

import com.example.training_tracker.data.models.ExerciseType
import com.example.training_tracker.data.models.MuscleGroups

data class ExerciseRequest(
    val id: String,
    val name: String,
    val exerciseType: ExerciseType,
    val muscleGroup: MuscleGroups
)