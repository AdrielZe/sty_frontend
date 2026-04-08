package com.example.training_tracker.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workoutHistories")
data class WorkoutHistory(
    @PrimaryKey @ColumnInfo(name = "id") val id: String = java.util.UUID.randomUUID().toString(),
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "completionDate") val completionDate: Long,
    @ColumnInfo(name = "exercises") val exercises: List<Exercise>
)