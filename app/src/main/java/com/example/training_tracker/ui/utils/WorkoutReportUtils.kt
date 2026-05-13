package com.example.training_tracker.ui.utils

import android.content.Context
import com.example.training_tracker.R
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseType
import com.example.training_tracker.ui.screens.workout_report.TotalWeightLiftedInfo
import com.example.training_tracker.ui.screens.workout_report.WorkoutDifficulty

private fun String.parseToDouble(): Double {
    return this.replace(",", ".").toDoubleOrNull() ?: 0.0
}

fun WorkoutDifficulty?.generateHeroTitle(context: Context): String {
    if (this == null) return context.getString(R.string.workout_report_default_title)

    val arrayId = when (this) {
        WorkoutDifficulty.EASY -> R.array.workout_difficulty_easy_phrases
        WorkoutDifficulty.MEDIUM -> R.array.workout_difficulty_medium_phrases
        WorkoutDifficulty.HARD -> R.array.workout_difficulty_hard_phrases
        WorkoutDifficulty.SUPER_HARD -> R.array.workout_difficulty_super_hard_phrases
    }

    return context.resources.getStringArray(arrayId).random()
}

fun generateTotalWeightedInfo(context: Context, exercises: List<Exercise>): TotalWeightLiftedInfo {
    val totalWeight = exercises.filter { it.type == ExerciseType.STRENGTH }.sumOf { exercise ->
        exercise.exerciseSets.sumOf { set ->
            val reps = set.reps.toDoubleOrNull() ?: 0.0
            val weight = set.weight.parseToDouble()
            reps * weight
        }
    }

    val title = generateTotalWeightedTitle(context, totalWeight)
    val image = generateTotalWeightedImage(totalWeight)
    val comparison = generateWeightComparison(context, totalWeight)

    return TotalWeightLiftedInfo(
        value = totalWeight,
        title = title,
        image = image,
        comparisonText = comparison
    )
}

fun generateTotalWeightedTitle(context: Context, totalWeight: Double): String {
    val stringId = when {
        totalWeight == 0.0 -> R.string.weight_title_0
        totalWeight < 1500.0 -> R.string.weight_title_1500
        totalWeight < 3000.0 -> R.string.weight_title_3000
        totalWeight < 4200.0 -> R.string.weight_title_4200
        totalWeight < 6000.0 -> R.string.weight_title_6000
        totalWeight < 10000.0 -> R.string.weight_title_10000
        else -> R.string.weight_title_legendary
    }

    return context.getString(stringId)
}

fun generateTotalWeightedImage(totalWeight: Double): Int {
    return when {
        totalWeight == 0.0 -> R.drawable.confused_0kg
        totalWeight < 1500.0 -> R.drawable.feather_1kg
        totalWeight < 3000.0 -> R.drawable.lifting_2kg
        totalWeight < 4200.0 -> R.drawable.strong_3k
        totalWeight < 6000.0 -> R.drawable.strong_4k
        totalWeight < 10000.0 -> R.drawable.strong_5k
        else -> R.drawable.strong_6k
    }
}

fun generateWeightComparison(context: Context, totalWeight: Double): String {
    val arrayId = when {
        totalWeight == 0.0 -> R.array.weight_comparison_0
        totalWeight < 1500.0 -> R.array.weight_comparison_1500
        totalWeight < 3000.0 -> R.array.weight_comparison_3000
        totalWeight < 4200.0 -> R.array.weight_comparison_4200
        totalWeight < 6000.0 -> R.array.weight_comparison_6000
        totalWeight < 10000.0 -> R.array.weight_comparison_10000
        totalWeight < 15000.0 -> R.array.weight_comparison_15000
        totalWeight < 20000.0 -> R.array.weight_comparison_20000
        totalWeight < 25000.0 -> R.array.weight_comparison_25000
        totalWeight < 30000.0 -> R.array.weight_comparison_30000
        else -> R.array.weight_comparison_legendary
    }

    val randomComparison = context.resources.getStringArray(arrayId).random()

    return if (totalWeight == 0.0) {
        randomComparison
    } else {
        context.getString(R.string.weight_comparison_template, randomComparison)
    }
}

fun countTotalSets(exercises: List<Exercise>): Int {
    val totalSets = exercises.sumOf { exercise -> exercise.exerciseSets.size }

    return totalSets
}

fun countTotalReps(exercises: List<Exercise>): Int {
    val totalReps = exercises
        .filter { it.type == ExerciseType.STRENGTH }
        .sumOf { exercise -> exercise.exerciseSets.sumOf { it.reps.toIntOrNull() ?: 0 } }

    return totalReps
}
