package com.example.training_tracker.ui.screens.home

import android.icu.text.DateFormat
import com.example.training_tracker.data.models.User
import com.example.training_tracker.data.models.Workout
import java.time.LocalDate

data class HomeUiState(
    val user: User? = null,
    val currentDate: String? = null,
    val todayWorkout: Workout? = null,
)