package com.example.training_tracker.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.training_tracker.ui.screens.workout_report.WorkoutDifficulty
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "workoutHistories")
data class WorkoutHistory(
    @PrimaryKey @ColumnInfo(name = "id") val id: String = java.util.UUID.randomUUID().toString(),
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "completionDate") val completionDate: LocalDate,
    @ColumnInfo(name = "completionTime") val completionTime: LocalTime? = null,
    @ColumnInfo(name = "exercises") val exercises: List<Exercise>,
    @ColumnInfo(name = "workoutId") val workoutId: String,
    @ColumnInfo(name = "difficulty") val difficulty: WorkoutDifficulty,
    @ColumnInfo(name = "durationMillis") val durationMillis: Long = 0L,
    @ColumnInfo(name = "records") val records: Records ?= null
)
