package com.example.training_tracker.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.training_tracker.ui.screens.workout_report.WorkoutDifficulty
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "workoutHistories")
data class
WorkoutHistory(
    @PrimaryKey @ColumnInfo(name = "id") val id: String = java.util.UUID.randomUUID().toString(),
    @ColumnInfo(name = "userId") val userId: String? = null,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "completionDate") val completionDate: LocalDate,
    @ColumnInfo(name = "completionTime") val completionTime: LocalTime? = null,
    // o endpoint GET /history devolve esse campo como "exerciseList", diferente
    // do resto da api (que usa "exercises"); alternate cobre a leitura sem mudar
    // o nome usado ao enviar dados de volta pro POST /history/all
    @ColumnInfo(name = "exercises") @SerializedName(value = "exercises", alternate = ["exerciseList"]) val exercises: List<Exercise>,
    @ColumnInfo(name = "workoutId") val workoutId: String,
    @ColumnInfo(name = "difficulty") val difficulty: WorkoutDifficulty,
    @ColumnInfo(name = "durationMillis") val durationMillis: Long = 0L,
    @ColumnInfo(name = "records") val records: Records ?= null,
    @ColumnInfo(name = "isCompleted") val isCompleted: Boolean = true,
    @ColumnInfo(name = "isSynced") val isSynced: Boolean = false
)
