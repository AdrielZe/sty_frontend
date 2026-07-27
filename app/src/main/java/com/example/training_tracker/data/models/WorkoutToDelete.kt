package com.example.training_tracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts_to_delete")
data class WorkoutToDelete (
    @PrimaryKey
    val workoutId: String
)