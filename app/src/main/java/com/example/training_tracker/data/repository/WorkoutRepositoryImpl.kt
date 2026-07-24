package com.example.training_tracker.data.repository

import android.content.Context
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.training_tracker.data.local.dao.WorkoutDao
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.remote.sync.SyncDataWorker
import com.example.training_tracker.data.remote.workout.WorkoutApi
import com.example.training_tracker.data.remote.workout.WorkoutRequest
import com.example.training_tracker.domain.repository.WorkoutRepository
import com.example.training_tracker.session_manager.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import java.time.DayOfWeek
import java.util.UUID

class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao,
    private val workoutApi: WorkoutApi,
    private val context: Context,
    private val sessionManager: SessionManager
): WorkoutRepository {
    override val workouts = workoutDao.getAllWorkouts()

    override suspend fun addWorkout(workout: Workout) {
        val isLoggedIn = sessionManager.isLoggedIn.first()
        workoutDao.insert(workout)

        if (isLoggedIn) {
            scheduleSync(context)
            try {
                val request = WorkoutRequest(
                    workoutId = UUID.fromString(workout.id),
                    userId = UUID.fromString(workout.userId),
                    workoutName = workout.name,
                    dayOfWeek = workout.dayOfWeek!!,
                    exercises = workout.exercises
                )
                workoutApi.createWorkout(request)

                workoutDao.insert(workout.copy(isSynced = true))

            } catch (e: Exception) {
                Log.e(
                    "Workout repo",
                    "Falha ao salvar na API na hora. Salvando localmente para sync futuro.",
                    e
                )
                workoutDao.insert(workout.copy(isSynced = false))
                scheduleSync(context)
            }
        }

    }

    override suspend fun updateWorkout(workout: Workout) {
        workoutDao.update(workout)
    }

    override suspend fun deleteWorkout(workout: Workout) {
        workoutDao.delete(workout)
    }

    override fun getWorkoutById(id: String) : Flow<Workout?> {
        return workouts.map { list ->
            list.find { it.id == id }
        }
    }

    override fun getWorkoutsByDay(day: DayOfWeek, userId: UUID?): Flow<List<Workout>> {
        return workouts.map { list ->
            list.filter { it.dayOfWeek == day }
        }.onStart {
            if (userId != null) {
                try {
                    val workoutResponse = workoutApi.getWorkoutByDay(userId, day)
                    val workouts = workoutResponse.map { response ->
                        Workout(
                            id = response.workoutId.toString(),
                            userId = response.userId.toString(),
                            name = response.workoutName,
                            dayOfWeek = response.dayOfWeek,
                            exercises = response.exercises
                        )
                    }
                    workoutDao.insertOrUpdateAll(workouts)
                } catch (e: Exception) {
                    Log.e("Workout repo", "Unable to fetch workouts from API", e)
                }
            }
        }
    }

    override fun getTodayWorkout(day: DayOfWeek): Flow<Workout?> {
        return workouts.map { list ->
            list.firstOrNull { it.dayOfWeek == day }
        }
    }

    override suspend fun deleteAllWorkouts() {
        workoutDao.deleteAllWorkouts()
    }

    override suspend fun getAllNotSyncedWorkouts() : List<Workout>{
        return workoutDao.getAllNotSyncedWorkouts()
    }

    override suspend fun updateWorkoutsWithCount(workouts: List<Workout>): Int {
        return workoutDao.updateWorkoutsWithCount(workouts)
    }

    private fun scheduleSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncDataWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "SyncPendingWorkouts", // Um nome único para essa tarefa
            ExistingWorkPolicy.REPLACE, // Se já tiver um sync na fila esperando internet, substitui por esse novo
            syncWorkRequest
        )
    }
}