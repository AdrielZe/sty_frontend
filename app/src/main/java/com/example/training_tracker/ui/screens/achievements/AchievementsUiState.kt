package com.example.training_tracker.ui.screens.conquistas

import androidx.annotation.DrawableRes
import com.example.training_tracker.data.models.MuscleGroups
import java.time.LocalDate

/**
 * UI state for the new "Conquistas" (Records v2) screen.
 * Kept separate from RecordsUiState on purpose so the existing RecordsScreen
 * keeps working untouched.
 *
 * The shapes below are aggregator outputs you can produce from your existing
 * WorkoutHistory + ExerciseDataAggregator. See [ConquistasAggregator] for the
 * sample contract.
 */
data class AchievementsUiState(
    val isLoading: Boolean = true,
    val totalPRs: Int = 0,
    val champion: Champion? = null,
    val thisMonth: MonthStats? = null,
    val timeline: List<PrEntry> = emptyList(),
    val medals: List<Medal> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val selectedYear: YearFilter = YearFilter.All,
    val selectedDay: Int? = null          // day-of-month selected in the heatmap
)

enum class YearFilter(val label: String, val year: Int?) {
    All("Todos", null),
    Y2026("2026", 2026),
    Y2025("2025", 2025),
    Y2024("2024", 2024);
}

/** The single heaviest absolute PR currently held. */
data class Champion(
    val exerciseName: String,
    val muscleGroup: MuscleGroups?,
    @DrawableRes val image: Int?,
    val weightKg: Double,
    val previousWeightKg: Double,
    val deltaKg: Double,
    val achievedOn: LocalDate,
    val sessionNumber: Int,
)

/** Current month rollup + heatmap of which days saw a PR. */
data class MonthStats(
    val monthLabel: String,        // "MAIO"
    val daysInMonth: Int,
    val today: Int,                // 1..daysInMonth
    val prDays: Set<Int>,          // days of month where any PR was set
    val prCount: Int,
    val exerciseCount: Int,
    val volumeDeltaPct: Int,       // % vs previous month
)

/** A single PR moment in chronological order (newest first). */
data class PrEntry(
    val date: LocalDate,
    val exerciseName: String,
    val muscleGroup: MuscleGroups?,
    val weightKg: Double,
    val previousWeightKg: Double,
    val deltaKg: Double,
    val sessionNumber: Int,
    val workoutName: String,
    val isCrown: Boolean,          // true if this entry is the all-time champion
)

/** A medallion for an exercise's all-time best — tiered by weight thresholds. */
data class Medal(
    val exerciseName: String,
    val abbreviation: String,      // 2-3 letters, e.g. "AG" / "SR"
    val muscleGroup: MuscleGroups?,
    @DrawableRes val image: Int?,
    val weightKg: Double,
    val tier: MedalTier,
)

enum class MedalTier { GOLD, SILVER, BRONZE }

/** A projected next milestone for an exercise (e.g. PR + small jump). */
data class Goal(
    val exerciseName: String,
    val muscleGroup: MuscleGroups?,
    val currentKg: Double,
    val targetKg: Double,
    val progressPct: Int,          // 0..100
    val etaLabel: String,          // "≈ 2 semanas"
)

/* ─────────────────────────────────────────────────────────────────────────────
 * AGGREGATOR — example sketch. Plug your real data (WorkoutHistory list) in
 * and emit a ConquistasUiState. The screen does not care HOW the aggregation
 * happens, only that it receives this shape.
 * ───────────────────────────────────────────────────────────────────────────── */
object ConquistasAggregator {

    private fun tierFor(weightKg: Double): MedalTier = when {
        weightKg >= 100.0 -> MedalTier.GOLD
        weightKg >= 50.0  -> MedalTier.SILVER
        else              -> MedalTier.BRONZE
    }

    /** Build a 2–3 letter abbreviation from an exercise name. */
    fun abbreviate(name: String): String {
        val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            parts.isEmpty()     -> "?"
            parts.size == 1     -> parts[0].take(2).uppercase()
            else                -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }

    /** Convenience helper to wrap a weight into a Medal. */
    fun toMedal(
        exerciseName: String,
        muscleGroup: MuscleGroups?,
        @DrawableRes image: Int?,
        weightKg: Double
    ) = Medal(
        exerciseName = exerciseName,
        abbreviation = abbreviate(exerciseName),
        muscleGroup = muscleGroup,
        image = image,
        weightKg = weightKg,
        tier = tierFor(weightKg)
    )
}
