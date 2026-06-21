package com.krishifarms.mobile.feature.farmer.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.krishifarms.mobile.feature.farmer.domain.repository.FarmerRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FarmerSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val farmerRepository: FarmerRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            farmerRepository.syncFarmers()
            Result.success()
        } catch (exception: Exception) {
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "farmer_sync"
        private const val MAX_RETRIES = 3
    }
}
