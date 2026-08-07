package com.example.training_tracker.data.repository

import android.util.Log
import com.example.training_tracker.data.local.dao.WorkoutHistoryDao
import com.example.training_tracker.data.models.WorkoutHistory
import com.example.training_tracker.data.remote.history.HistoryApi
import com.example.training_tracker.data.remote.history.HistoryRequestDto
import com.example.training_tracker.data.remote.history.HistoryResponseDto
import com.example.training_tracker.data.remote.sync.SyncConfiguration
import com.example.training_tracker.data.remote.sync.SyncManager
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import com.example.training_tracker.session_manager.SessionManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import kotlin.math.log

class WorkoutHistoryImpl(
    private val workoutHistoryDao: WorkoutHistoryDao,
    private val historyApi: HistoryApi,
    private val sessionManager: SessionManager,
    private val syncManager: SyncManager
) : WorkoutHistoryRepository {
    override val workoutHistories = workoutHistoryDao.getAllWorkoutHistories()

    override fun getWorkoutById(id: String): Flow<WorkoutHistory?> {
        return workoutHistoryDao.getWorkoutById(id)
    }

    override fun getHistories(userId: String): Flow<List<WorkoutHistory>> {
        return workoutHistoryDao.getAllWorkoutHistories().onStart {
            if (sessionManager.isLoggedIn.first()) {
                try {
                    val historyResponse = historyApi.getHistories(userId)

                    val histories = historyResponse.histories

                    val syncedHistories = histories.map { it.copy(exercises = it.exercises ?: emptyList(), isSynced = true) }
                    workoutHistoryDao.insertOrUpdateAll(syncedHistories)
                } catch (e: Exception) {
                    Log.e("Histories repo", "Unable to fetch history from API", e)
                }
            }
        }
    }

    override suspend fun addWorkoutHistory(workoutHistory: WorkoutHistory) {
        workoutHistoryDao.insert(workoutHistory)

        if (sessionManager.isLoggedIn.first()) {
            if (!workoutHistory.isSynced) {
                try {
                    historyApi.createHistory(
                        HistoryRequestDto(
                            id = UUID.fromString(workoutHistory.id),
                            name = workoutHistory.name,
                            isCompleted = workoutHistory.isCompleted,
                            completionDate = workoutHistory.completionDate.toString(),
                            completionTime = (workoutHistory.completionTime
                                ?: LocalTime.now()).toString(),
                            userId = UUID.fromString(workoutHistory.userId),
                            durationMillis = workoutHistory.durationMillis,
                            workoutId = UUID.fromString(workoutHistory.workoutId),
                            exercises = workoutHistory.exercises,
                        )
                    )
                    print("HISTORY SAVED ADD: $workoutHistory")
                    workoutHistoryDao.insert(workoutHistory.copy(isSynced = true))
                } catch (e: Exception) {
                    println("DEBUG ADD WORKOUT HISTORY: ${workoutHistory.exercises}")
                    workoutHistoryDao.insert(workoutHistory.copy(isSynced = false))
                    syncManager.scheduleGlobalSync()
                    Log.e("Add workout history: ", "Error creating history", e)
                }
            }
        }
    }

    override suspend fun getAllNotSyncedHistories(): List<WorkoutHistory> {
        return workoutHistoryDao.getAllNotSyncedHistories()
    }
    override suspend fun deleteWorkoutHistory(workoutHistory: WorkoutHistory) {
        workoutHistoryDao.delete(workoutHistory)
    }

    override suspend fun updateWorkoutHistory(workoutHistory: WorkoutHistory) {
        workoutHistoryDao.update(workoutHistory)
    }

    override suspend fun deleteAll() {
        workoutHistoryDao.deleteAll()
    }

    override suspend fun insert(workoutHistory: WorkoutHistory) : Long {
        return workoutHistoryDao.insert(workoutHistory)
    }

    override suspend fun updateAll(history: List<WorkoutHistory>) {
        workoutHistoryDao.updateAll(history)
    }
}
