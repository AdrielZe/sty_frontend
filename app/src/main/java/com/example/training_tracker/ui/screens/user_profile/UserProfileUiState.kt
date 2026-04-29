package com.example.training_tracker.ui.screens.user_profile

import com.example.training_tracker.data.models.User
import com.example.training_tracker.data.models.MuscleGroups

data class UserStats(
    val mostTrainedMuscleGroup: MuscleGroups? = null,
    val maxVolume: Double = 0.0,
    val totalWorkouts: Int = 0,
    val totalSets: Int = 0,
    val heaviestExerciseName: String? = null,
    val heaviestWeight: Double = 0.0
)

sealed interface UserProfileUiState {
    object Loading : UserProfileUiState
    data class Success(
        val user: User,
        val stats: UserStats
    ) : UserProfileUiState
    data class Error(val message: String) : UserProfileUiState
}
