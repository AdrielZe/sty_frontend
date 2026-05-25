package com.example.training_tracker.ui.screens.exercise_data

import com.example.training_tracker.data.models.ExerciseType
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.Technique
import com.example.training_tracker.data.models.WorkoutHistory
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

/** A single completed set logged for an exercise. */
data class LoggedSet(
    val setNumber: Int,
    val reps: Int,
    val weight: Double,
    val technique: Technique
) {
    val volume: Double get() = reps * weight
}

/** One occurrence of an exercise within a completed workout, with all its sets. */
data class ExerciseSession(
    val date: LocalDate,
    val time: LocalTime?,
    val workoutName: String,
    val sets: List<LoggedSet>
) {
    val topWeight: Double get() = sets.maxOfOrNull { it.weight } ?: 0.0
    val totalVolume: Double get() = sets.sumOf { it.volume }
    val totalReps: Int get() = sets.sumOf { it.reps }
    val setCount: Int get() = sets.size

    /** Epley estimated one-rep max — the heaviest single-set projection of the session. */
    val estimatedOneRepMax: Double
        get() = sets.maxOfOrNull { s ->
            if (s.reps <= 1) s.weight else s.weight * (1.0 + s.reps / 30.0)
        } ?: 0.0
}

/** The full chronological log of one exercise across every workout it ever appeared in. */
data class ExerciseLog(
    val name: String,
    val muscleGroup: MuscleGroups?,
    /** Sessions ordered oldest -> newest. */
    val sessions: List<ExerciseSession>
) {
    val sessionCount: Int get() = sessions.size
    val firstSession: ExerciseSession? get() = sessions.firstOrNull()
    val lastSession: ExerciseSession? get() = sessions.lastOrNull()

    val allTimePR: Double get() = sessions.maxOfOrNull { it.topWeight } ?: 0.0
    val bestVolume: Double get() = sessions.maxOfOrNull { it.totalVolume } ?: 0.0
    val bestOneRepMax: Double get() = sessions.maxOfOrNull { it.estimatedOneRepMax } ?: 0.0
    val totalVolumeLifted: Double get() = sessions.sumOf { it.totalVolume }

    /** Newest-first weight series — matches the order EvolutionChart/MiniSparkline expect. */
    val weightSeriesNewestFirst: List<Double> get() = sessions.map { it.topWeight }.reversed()
    val volumeSeriesNewestFirst: List<Double> get() = sessions.map { it.totalVolume }.reversed()
    val oneRepMaxSeriesNewestFirst: List<Double> get() = sessions.map { it.estimatedOneRepMax }.reversed()

    /** Average of the session-to-session percentage changes of top weight. */
    val weightProgressPct: Double?
        get() = averageProgress(sessions.map { it.topWeight })

    /** Average of the session-to-session percentage changes of total volume. */
    val volumeProgressPct: Double?
        get() = averageProgress(sessions.map { it.totalVolume })

    /** Percentage change of top weight from the first session to the last. */
    val weightTotalProgressPct: Double?
        get() = if (sessions.size < 2) null
        else firstToLastProgress(firstSession?.topWeight, lastSession?.topWeight)

    /** Percentage change of total volume from the first session to the last. */
    val volumeTotalProgressPct: Double?
        get() = if (sessions.size < 2) null
        else firstToLastProgress(firstSession?.totalVolume, lastSession?.totalVolume)
}

/**
 * Average of the percentage changes between each pair of consecutive values
 * (chronological order). Returns null when there is no pair to compare.
 */
private fun averageProgress(chronologicalValues: List<Double>): Double? {
    if (chronologicalValues.size < 2) return null
    val changes = chronologicalValues.zipWithNext().mapNotNull { (previous, current) ->
        if (previous > 0.0) (current - previous) / previous * 100.0 else null
    }
    return if (changes.isEmpty()) null else changes.average()
}

/** Percentage change between the first and last value. Returns null without a valid pair. */
private fun firstToLastProgress(first: Double?, last: Double?): Double? {
    if (first == null || last == null || first <= 0.0) return null
    return (last - first) / first * 100.0
}

/**
 * Resultado do cálculo de destaque para o exercício da tela de detalhe.
 *
 * @param todayVolume   Volume total feito hoje neste exercício.
 * @param volumeChangePct Variação percentual em relação à última sessão anterior.
 *                        Null = primeira vez que o exercício foi feito.
 * @param isTopHighlight  True se este exercício teve o maior aumento percentual (ou maior
 *                        volume absoluto quando não há histórico anterior) entre todos os
 *                        exercícios realizados hoje.
 */
data class TodayHighlightData(
    val todayVolume: Double,
    val volumeChangePct: Double?,
    val todayPeak: Double,
    val peakChangePct: Double?,
    val isTopHighlight: Boolean
)

object ExerciseDataAggregator {

    /**
     * Builds one [ExerciseLog] per distinct strength exercise found across all workout histories.
     * Only sets with a parseable positive weight and positive reps are counted.
     */
    fun build(histories: List<WorkoutHistory>): List<ExerciseLog> {
        data class Acc(val displayName: String, var muscleGroup: MuscleGroups?, val sessions: MutableList<ExerciseSession>)

        val byKey = LinkedHashMap<String, Acc>()

        histories.forEach { history ->
            history.exercises
                .filter { it.type == ExerciseType.STRENGTH }
                .forEach { exercise ->
                    val sets = exercise.exerciseSets.mapNotNull { set ->
                        val reps = set.reps.trim().toIntOrNull()
                        val weight = set.weight.trim().replace(",", ".").toDoubleOrNull()
                        if (reps != null && reps > 0 && weight != null && weight > 0.0) {
                            LoggedSet(
                                setNumber = set.set,
                                reps = reps,
                                weight = weight,
                                technique = set.technique
                            )
                        } else null
                    }
                    if (sets.isEmpty()) return@forEach

                    val key = exercise.name.trim().uppercase(Locale.getDefault())
                    val acc = byKey.getOrPut(key) {
                        Acc(exercise.name.trim(), exercise.muscleGroup, mutableListOf())
                    }
                    if (acc.muscleGroup == null && exercise.muscleGroup != null) {
                        acc.muscleGroup = exercise.muscleGroup
                    }
                    acc.sessions.add(
                        ExerciseSession(
                            date = history.completionDate,
                            time = history.completionTime,
                            workoutName = history.name,
                            sets = sets.sortedBy { it.setNumber }
                        )
                    )
                }
        }

        return byKey.values.map { acc ->
            ExerciseLog(
                name = acc.displayName,
                muscleGroup = acc.muscleGroup,
                sessions = acc.sessions.sortedWith(
                    compareBy({ it.date }, { it.time ?: LocalTime.MIN })
                )
            )
        }
    }
}

private val weightFormat: NumberFormat
    get() = NumberFormat.getInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 1
        minimumFractionDigits = 0
    }

fun formatWeight(value: Double): String = weightFormat.format(value)

fun formatVolume(value: Double): String {
    return if (value >= 1000) {
        NumberFormat.getInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 1
            minimumFractionDigits = 0
        }.format(value / 1000.0) + " mil kg"
    } else {
        weightFormat.format(value) + " kg"
    }
}
