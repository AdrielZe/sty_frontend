package com.example.training_tracker.data.remote.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncDataWorker(
    val appContext: Context,
    val workerParams: WorkerParameters,
    val syncConfiguration: SyncConfiguration
): CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            syncConfiguration.syncPendingWorkouts()

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}