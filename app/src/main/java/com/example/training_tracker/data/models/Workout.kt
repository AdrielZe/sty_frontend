package com.example.training_tracker.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey @ColumnInfo(name = "id") val id: String = java.util.UUID.randomUUID().toString(),
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "exercises") val exercises: List<Exercise> = emptyList(),
    @ColumnInfo(name = "isCompleted") val isCompleted: Boolean = false,
    @ColumnInfo(name = "isOnGoing") val isOnGoing: Boolean = false,
    @ColumnInfo(name = "isPaused") val isPaused: Boolean = false,
    @ColumnInfo(name = "dayOfWeek") val dayOfWeek: DayOfWeek? = null,
    @ColumnInfo(name = "completionDate") val completionDate: LocalDate? = null,
    @ColumnInfo(name = "startTime") val startTime: Long? = null,
    @ColumnInfo(name = "accumulatedTime") val accumulatedTime: Long = 0L,
    @ColumnInfo(name = "historyId") val historyId: String? = null,
    @ColumnInfo(name = "estimatedTime") val estimatedTime: Int = exercises.size * 10,
    @ColumnInfo(name = "progress") val progress: Float = 0f
)
