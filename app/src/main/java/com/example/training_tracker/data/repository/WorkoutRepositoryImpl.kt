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
import com.example.training_tracker.data.local.dao.WorkoutToDeleteDao
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.WorkoutToDelete
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
    private val workoutToDeleteDao: WorkoutToDeleteDao,
    private val sessionManager: SessionManager
): WorkoutRepository {
    override val workouts = workoutDao.getAllWorkouts()

    override suspend fun addWorkout(workout: Workout) {
        val isLoggedIn = sessionManager.isLoggedIn.first()
        workoutDao.insert(workout)

        if (isLoggedIn) {
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
        val isLoggedIn = sessionManager.isLoggedIn.first()
        workoutDao.update(workout.copy(isSynced = false))

        if (isLoggedIn) {
            try {
                workoutApi.createWorkout(WorkoutRequest(
                    workoutId = UUID.fromString(workout.id),
                    userId = UUID.fromString(workout.userId),
                    workoutName = workout.name,
                    dayOfWeek = workout.dayOfWeek!!,
                    exercises = workout.exercises
                ))
                workoutDao.update(workout.copy(isSynced = true))
            } catch (e: Exception) {
                Log.e("Update workout:", "Error updating workout: ", e)
                scheduleSync(context)
            }
        }
    }

    override suspend fun deleteWorkoutById(workoutId: String) {
        workoutToDeleteDao.insert(WorkoutToDelete(workoutId))
        workoutDao.deleteById(workoutId)

        if (sessionManager.isLoggedIn.first()) {
            try {
                workoutApi.deleteWorkout(UUID.fromString(workoutId))
                workoutToDeleteDao.delete(workoutId)
            } catch (e: Exception) {
                Log.e("Delete workouts", "Error deleting workouts in remote DB", e)
                scheduleSync(context)
            }
        }
    }

    override fun getWorkoutById(id: String) : Flow<Workout?> {
        return workouts.map { list ->
            list.find { it.id == id }
        }.onStart {
            val existingLocal = workouts.map { list -> list.find { it.id == id } }.first()

            // enquanto já existe uma sessão local em andamento (ex: freestyle workout com
            // exercícios sendo adicionados nesse instante), o local é a fonte da verdade,
            // buscar o remoto aqui sobrescreveria edições que ainda não foram sincronizadas.
            if (existingLocal != null && existingLocal.isOnGoing) return@onStart

            if (sessionManager.isLoggedIn.first()) {
                try {
                    val workoutResponse = workoutApi.getWorkoutById(UUID.fromString(id))

                    val workout = Workout(
                            id = workoutResponse.workoutId.toString(),
                            name = workoutResponse.workoutName,
                            exercises = workoutResponse.exercises,
                            dayOfWeek = workoutResponse.dayOfWeek,
                            userId = workoutResponse.userId.toString(),
                            // O backend não guarda estado de sessão (isOnGoing/startTime); um
                            // freestyle workout só existe no remoto enquanto está em andamento,
                            // já que ao ser concluído os dados vão para o histórico.
                            isOnGoing = id == workoutResponse.userId.toString()
                    )

                    println("WORKOUT LOADED $workout")

                    workoutDao.insertOrUpdate(workout)
                } catch (e: Exception) {
                    Log.e("Get workout by id:", "Workout was not found", e)
                }

            }
        }
    }

    override fun getWorkoutsByDay(day: DayOfWeek, userId: UUID): Flow<List<Workout>> {
        return workouts.map { list ->
            list.filter { it.dayOfWeek == day }
        }.onStart {
            if (sessionManager.isLoggedIn.first()) {
                try {
                    val workoutResponse = workoutApi.getWorkoutByDay(userId, day)
                    val workouts = workoutResponse
                        // O freestyle workout é sincronizado exclusivamente via getWorkoutById,
                        // que reconstrói isOnGoing corretamente; não sobrescrever aqui.
                        .filter { it.workoutId.toString() != it.userId.toString() }
                        .map { response ->
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

    override suspend fun getPendingWorkoutsToDelete(): List<WorkoutToDelete> {
        return workoutToDeleteDao.getAllPendingDeletes();
    }

    private fun scheduleSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncDataWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "SyncPendingWorkouts",
            ExistingWorkPolicy.REPLACE, // se já tiver um sync na fila esperando, substitui por esse novo
            syncWorkRequest
        )
    }
}