package com.example.training_tracker.ui.screens.workout_report

import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.Records
import java.time.LocalDate
import java.time.LocalTime

data class TotalWeightLiftedInfo(
    val title: String,
    val value: Double,
    val image: Int,
    val comparisonText: String
)

data class Records(
    val recordList: List<Record>
)

data class Record(
    val title: String,
    val description: String,
    val icon: Int,
    val image: Int
)
enum class WorkoutDifficulty{
    EASY,
    MEDIUM,
    HARD,
    SUPER_HARD
}
data class WorkoutReportUiState(
    val isLoading: Boolean = false,
    val workoutName: String = "",
    val workoutDifficulty: WorkoutDifficulty? = null,
    val heroSectionTitle: String? = "",
    val completionDate: LocalDate? = LocalDate.now(),
    val completionTime: LocalTime? = null,
    val totalWeightLiftedInfo: TotalWeightLiftedInfo? = null,
    val totalSets: Int = 0,
    val totalReps: Int = 0,
    val totalMinutes: Int = 0,
    val records: Records ?= null,
    val exercises: List<Exercise> = emptyList(),
    val caloriesBurned: Int? = null,
    /** Novos recordes de peso batidos neste treino: nome do exercício → peso máximo (kg) */
    val sessionPRs: Map<String, Double> = emptyMap(),
)