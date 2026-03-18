package com.example.training_tracker

import androidx.compose.runtime.Composable
import com.example.training_tracker.ui.screens.create_workout.LoginScreen

@Composable
fun GymTrackerApp() {
    val loggedIn = true

    if (loggedIn) {
        GymTrackerNavHost()
    } else {
        LoginScreen()
    }
}