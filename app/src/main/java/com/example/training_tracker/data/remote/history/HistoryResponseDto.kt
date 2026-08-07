package com.example.training_tracker.data.remote.history

import com.example.training_tracker.data.models.WorkoutHistory

data class HistoryResponseDto(
    val histories: List<WorkoutHistory>
)
