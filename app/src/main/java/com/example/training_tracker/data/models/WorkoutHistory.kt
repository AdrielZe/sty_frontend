package com.example.training_tracker.data.models

import android.adservices.adid.AdId
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.training_tracker.ui.screens.workout_report.WorkoutDifficulty
import java.time.LocalDate

@Entity(tableName = "workoutHistories")
data class WorkoutHistory(
    @PrimaryKey @ColumnInfo(name = "id") val id: String = java.util.UUID.randomUUID().toString(),
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "completionDate") val completionDate: LocalDate,
    @ColumnInfo(name = "exercises") val exercises: List<Exercise>,
    @ColumnInfo(name = "workoutId") val workoutId: String,
    @ColumnInfo(name = "difficulty") val difficulty: WorkoutDifficulty
)