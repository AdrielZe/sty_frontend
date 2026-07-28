package com.example.training_tracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.training_tracker.service.WorkoutTimerService
import com.example.training_tracker.session_manager.MainViewModel
import com.example.training_tracker.ui.theme.Training_trackerTheme

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNotificationIntent(intent)
        setContent {
            Training_trackerTheme {
                GymTrackerApp(mainViewModel = mainViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val workoutId = intent?.getStringExtra(WorkoutTimerService.NOTIFICATION_WORKOUT_ID_KEY)
        if (!workoutId.isNullOrBlank()) {
            mainViewModel.onNotificationWorkoutTapped(workoutId)
        }
    }
}
