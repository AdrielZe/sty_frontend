package com.example.training_tracker.data.remote.workout

import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.Workout
import java.time.DayOfWeek
import java.util.UUID

data class WorkoutResponse(
    val workoutId: UUID,
    val userId: UUID,
    val workoutName: String,
    val dayOfWeek: DayOfWeek,
    val exercises: List<Exercise>
)
