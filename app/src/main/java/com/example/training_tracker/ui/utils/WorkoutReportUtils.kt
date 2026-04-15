package com.example.training_tracker.ui.utils

import android.content.Context
import com.example.training_tracker.R
import com.example.training_tracker.ui.screens.workout_report.WorkoutDifficulty

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