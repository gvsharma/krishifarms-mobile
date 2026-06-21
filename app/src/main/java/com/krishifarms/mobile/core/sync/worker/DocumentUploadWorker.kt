package com.krishifarms.mobile.core.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.krishifarms.mobile.feature.document.domain.repository.DocumentRepository
import com.krishifarms.mobile.feature.document.domain.repository.DocumentUploadResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@HiltWorker
class DocumentUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val documentRepository: DocumentRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val documentId = inputData.getString(KEY_DOCUMENT_ID) ?: return Result.failure()
        return when (documentRepository.uploadDocument(documentId)) {
            is DocumentUploadResult.Success -> Result.success()
            is DocumentUploadResult.Retry -> Result.retry()
            is DocumentUploadResult.Failure -> Result.failure()
        }
    }

    companion object {
        const val KEY_DOCUMENT_ID = "document_id"
        const val WORK_NAME_PREFIX = "document_upload_"
    }
}

@Singleton
class DocumentUploadScheduler @Inject constructor(
    private val workManager: WorkManager,
) {
    fun scheduleUpload(documentId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<DocumentUploadWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 60, TimeUnit.SECONDS)
            .setInputData(
                androidx.work.Data.Builder()
                    .putString(DocumentUploadWorker.KEY_DOCUMENT_ID, documentId)
                    .build(),
            )
            .addTag(DocumentUploadWorker.WORK_NAME_PREFIX + documentId)
            .build()

        workManager.enqueueUniqueWork(
            DocumentUploadWorker.WORK_NAME_PREFIX + documentId,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}
