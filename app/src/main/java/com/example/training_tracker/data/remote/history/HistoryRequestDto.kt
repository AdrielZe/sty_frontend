package com.example.training_tracker.data.remote.history

import com.example.training_tracker.data.models.Exercise
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class HistoryRequestDto(
    val id: UUID,
    val name: String,
    val userId: UUID,
    val isCompleted: Boolean,
    val completionDate: String,
    val completionTime: String,
    val durationMillis: Long,
    val workoutId: UUID,
    val exercises: List<Exercise>,
){}