package com.example.training_tracker.data.remote.history

import com.example.training_tracker.data.models.WorkoutHistory
import java.util.UUID

data class AllHistoryRequestDto(
  val userId: UUID,
  val histories: List<WorkoutHistory>
){}