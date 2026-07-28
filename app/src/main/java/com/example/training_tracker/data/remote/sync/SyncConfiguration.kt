package com.example.training_tracker.data.remote.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.training_tracker.data.local.dao.UserDao
import com.example.training_tracker.data.local.dao.WorkoutDao
import com.example.training_tracker.data.local.dao.WorkoutToDeleteDao
import com.example.training_tracker.data.remote.auth.AuthApi
import com.example.training_tracker.data.remote.user.UserApi
import com.example.training_tracker.data.remote.user.WeeklyGoalRequestDto
import com.example.training_tracker.data.remote.workout.WorkoutApi
import com.example.training_tracker.data.remote.workout.WorkoutRequest
import com.example.training_tracker.domain.repository.UserRepository
import com.example.training_tracker.domain.repository.WorkoutRepository
import com.example.training_tracker.ui.screens.create_workout.LoginScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.internal.notify
import java.util.UUID

data class SyncConfiguration(
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val workoutApi: WorkoutApi,
    private val workoutToDeleteDao: WorkoutToDeleteDao,
    private val userDao: UserDao,
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

    suspend fun syncDeletedWorkouts() {
        val workoutsToDelete = workoutRepository.getPendingWorkoutsToDelete();

        workoutsToDelete.forEach { workout ->
            try {
                workoutApi.deleteWorkout(UUID.fromString(workout.workoutId))
                workoutToDeleteDao.delete(workout.workoutId)
                Log.d("Sync Deleted workouts:", "Successfully synced and deleted workout ${workout.workoutId}")
            } catch (e: Exception) {
                Log.e("Sync Deleted workouts:", "Error syncing", e)
            }
        }
    }

    suspend fun syncUserData() {
        val user = userRepository.getUser().firstOrNull() ?: return

        if (!user.isSynced && user.weeklyGoal != null) {
            try {
                userApi.setUserWeeklyGoal(UUID.fromString(user.id), WeeklyGoalRequestDto(weeklyGoal = user.weeklyGoal))
                userDao.upsertUser(user.copy(isSynced = true))
                Log.d("Sync Weekly Goal", "Successfully synced weekly goal")
            } catch (e: Exception) {
                Log.e("Sync Weekly Goal", "Error syncing weekly goal", e)
            }
        }
    }
}