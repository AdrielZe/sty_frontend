package com.example.training_tracker.data.remote.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.training_tracker.data.remote.auth.AuthApi
import com.example.training_tracker.data.remote.user.UserApi
import com.example.training_tracker.data.remote.workout.WorkoutApi
import com.example.training_tracker.data.remote.workout.WorkoutRequest
import com.example.training_tracker.domain.repository.UserRepository
import com.example.training_tracker.domain.repository.WorkoutRepository
import java.util.UUID

data class SyncConfiguration(
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val workoutApi: WorkoutApi,
    private val authApi: AuthApi,
    private val userApi: UserApi,
) {
    suspend operator fun invoke(userId: String) {
        userRepository.fetchUserProfileFromRemote(UUID.fromString(userId))
    }

    suspend fun syncPendingWorkouts() {
        val workoutsToSync = workoutRepository.getAllNotSyncedWorkouts()

        if (workoutsToSync.isNotEmpty()) {
            val workoutsRequest = workoutsToSync.map { workout ->
                println("user id workout is: ${workout.userId}")

                WorkoutRequest(
                    workoutId = UUID.fromString(workout.id),
                    userId = UUID.fromString(workout.userId),
                    workoutName = workout.name,
                    dayOfWeek = workout.dayOfWeek!!,
                    exercises = workout.exercises
                )
            }

            try {
                workoutApi.createWorkouts(workoutsRequest)
                val syncedWorkouts = workoutsToSync.map { workout -> workout.copy(isSynced = true)}
                val syncedWorkoutsCount = workoutRepository.updateWorkoutsWithCount(syncedWorkouts)
                Log.d("Workout Sync:", "$syncedWorkoutsCount workout(s) updated successfully.")
            } catch (e: Exception) {
                Log.e("Workout Sync:", "Failed to sync workouts:", e)
            }
        }

    }
}