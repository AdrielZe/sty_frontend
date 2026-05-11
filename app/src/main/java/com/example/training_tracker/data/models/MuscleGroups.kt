package com.example.training_tracker.data.models

import androidx.annotation.StringRes
import com.example.training_tracker.R

enum class MuscleGroups(@StringRes val resId: Int) {
    CHEST(R.string.muscle_chest),
    BACK(R.string.muscle_back),
    TRICEPS(R.string.muscle_triceps),
    BICEPS(R.string.muscle_biceps),
    SHOULDERS(R.string.muscle_shoulders),
    QUADRICEPS(R.string.quadriceps),
    HAMSTRINGS(R.string.posteriores),
    CALF(R.string.panturrilha),
    GLUTE(R.string.gluteo),
    ABS(R.string.muscle_abs)
}