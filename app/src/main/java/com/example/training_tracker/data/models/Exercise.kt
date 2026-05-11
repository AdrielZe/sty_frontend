package com.example.training_tracker.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise (
    @PrimaryKey @ColumnInfo(name = "id") val id: String = java.util.UUID.randomUUID().toString(),
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "muscleGroup") val muscleGroup: MuscleGroups? = null,
    @ColumnInfo(name = "exerciseSets") val exerciseSets: List<ExerciseSet> = listOf(ExerciseSet(1, "", "")),
    @ColumnInfo(name = "isDefault") val isDefault: Boolean = false,
    @ColumnInfo(name = "isCompleted") val isCompleted: Boolean = false,
    @ColumnInfo(name = "technique") val technique: String = "NONE",
    @ColumnInfo(name = "setsCompleted") val setsCompleted: Int = 1,
    @ColumnInfo(name = "weightRecord") val weightRecord: Int = 0
)