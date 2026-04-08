package com.example.training_tracker.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey @ColumnInfo(name = "id") val id: String = java.util.UUID.randomUUID().toString(),
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "exercises") val exercises: List<Exercise> = emptyList(),
    @ColumnInfo(name = "isCompleted") val isCompleted: Boolean = false,
    @ColumnInfo(name = "isOnGoing") val isOnGoing: Boolean = false,
    @ColumnInfo(name = "dayOfWeek") val dayOfWeek: DayOfWeek? = null,
    @ColumnInfo(name = "completionDate") val completionDate: Long? = null,
)