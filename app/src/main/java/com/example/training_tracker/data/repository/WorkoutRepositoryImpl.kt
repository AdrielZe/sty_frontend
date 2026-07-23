package com.example.training_tracker.data.repository

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.training_tracker.data.local.dao.UserDao
import com.example.training_tracker.data.local.dao.WorkoutDao
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.remote.sync.SyncConfiguration
import com.example.training_tracker.data.remote.sync.SyncDataWorker
import com.example.training_tracker.data.remote.user.UserApi
import com.example.training_tracker.data.remote.workout.WorkoutApi
import com.example.training_tracker.data.remote.workout.WorkoutRequest
import com.example.training_tracker.data.remote.workout.WorkoutResponse
import com.example.training_tracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import java.time.DayOfWeek
import java.util.UUID

class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao,
    private val workoutApi: WorkoutApi,
    private val context: Context
): WorkoutRepository {
    override val workouts = workoutDao.getAllWorkouts()

    override suspend fun addWorkout(workout: Workout, userId: UUID?) {
        workoutDao.insert(workout)
        scheduleSync(context)

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

        // 2. Prepara o pedido de trabalho (OneTime = roda uma vez por chamada)
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncDataWorker>()
            .setConstraints(constraints)
            .build()

        // 3. Coloca na fila do sistema
        WorkManager.getInstance(context).enqueueUniqueWork(
            "SyncPendingWorkouts", // Um nome único para essa tarefa
            ExistingWorkPolicy.REPLACE, // Se já tiver um sync na fila esperando internet, substitui por esse novo
            syncWorkRequest
        )
    }
}