package com.example.training_tracker.data.remote.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.training_tracker.GymTrackerApplication

class SyncDataWorker(
    val appContext: Context,
    val workerParams: WorkerParameters,
): CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val application = applicationContext as GymTrackerApplication
        val syncConfig = application.container.syncConfiguration

        return try {
            syncConfig.syncDeletedWorkouts()
            syncConfig.syncPendingWorkouts()
            syncConfig.syncUserData()

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncDataWorker Debug:", "Erro ao syncar dados", e)
            Result.retry()
        }
    }
}