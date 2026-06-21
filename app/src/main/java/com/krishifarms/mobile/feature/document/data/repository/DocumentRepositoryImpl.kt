package com.krishifarms.mobile.feature.document.data.repository

import com.krishifarms.mobile.core.common.DispatcherProvider
import com.krishifarms.mobile.core.common.IdGenerator
import com.krishifarms.mobile.core.common.NetworkMonitor
import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.dao.DocumentDao
import com.krishifarms.mobile.core.database.entity.DocumentEntity
import com.krishifarms.mobile.core.database.entity.SyncMetadata
import com.krishifarms.mobile.core.network.NetworkResult
import com.krishifarms.mobile.core.network.safeApiCall
import com.krishifarms.mobile.core.sync.worker.DocumentUploadScheduler
import com.krishifarms.mobile.core.util.DocumentFileManager
import com.krishifarms.mobile.feature.document.data.mapper.toDomain
import com.krishifarms.mobile.feature.document.data.remote.DocumentApi
import com.krishifarms.mobile.feature.document.data.remote.dto.DocumentDto
import com.krishifarms.mobile.feature.document.domain.model.Document
import com.krishifarms.mobile.feature.document.domain.model.DocumentType
import com.krishifarms.mobile.feature.document.domain.repository.AddDocumentInput
import com.krishifarms.mobile.feature.document.domain.repository.DocumentRepository
import com.krishifarms.mobile.feature.document.domain.repository.DocumentUploadResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao,
    private val documentApi: DocumentApi,
    private val documentFileManager: DocumentFileManager,
    private val documentUploadScheduler: DocumentUploadScheduler,
    private val networkMonitor: NetworkMonitor,
    private val dispatchers: DispatcherProvider,
    private val json: Json,
) : DocumentRepository {

    override fun observeDocument(id: String): Flow<Document?> =
        documentDao.observeById(id).map { entity -> entity?.toDomain() }

    override fun observeByType(type: DocumentType): Flow<List<Document>> =
        documentDao.observeByType(type).map { entities -> entities.map { it.toDomain() } }

    override fun observeByEntity(entityType: String?, entityId: String?): Flow<List<Document>> =
        documentDao.observeByEntity(entityType, entityId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getDocument(id: String): Document? =
        withContext(dispatchers.io) {
            documentDao.getById(id)?.toDomain()
        }

    override suspend fun addDocument(input: AddDocumentInput): Result<String> =
        withContext(dispatchers.io) {
            runCatching {
                val savedFile = when {
                    input.sourceUri != null -> documentFileManager.saveFromUri(input.sourceUri, input.type)
                    !input.sourcePath.isNullOrBlank() -> documentFileManager.saveFromPath(input.sourcePath, input.type)
                    else -> error("Document source is required")
                }

                val documentId = IdGenerator.newLocalId()
                val now = System.currentTimeMillis()
                val entity = DocumentEntity(
                    id = documentId,
                    documentType = input.type,
                    localPath = savedFile.path,
                    fileName = savedFile.fileName,
                    mimeType = savedFile.mimeType,
                    sizeBytes = savedFile.sizeBytes,
                    createdAt = now,
                    linkedEntityType = input.linkedEntityType,
                    linkedEntityId = input.linkedEntityId,
                    sync = SyncMetadata(
                        syncStatus = SyncStatus.PENDING_CREATE,
                        localUpdatedAt = now,
                    ),
                )
                documentDao.upsert(entity)
                documentUploadScheduler.scheduleUpload(documentId)

                if (networkMonitor.isOnline()) {
                    when (uploadDocument(documentId)) {
                        is DocumentUploadResult.Failure -> Unit
                        else -> Unit
                    }
                }

                documentId
            }
        }

    override suspend fun uploadDocument(documentId: String): DocumentUploadResult =
        withContext(dispatchers.io) {
            val entity = documentDao.getById(documentId)
                ?: return@withContext DocumentUploadResult.Failure("Document not found")

            if (entity.sync.syncStatus == SyncStatus.SYNCED && entity.remoteUrl != null) {
                return@withContext DocumentUploadResult.Success
            }

            if (!networkMonitor.isOnline()) {
                documentUploadScheduler.scheduleUpload(documentId)
                return@withContext DocumentUploadResult.Retry
            }

            val file = File(entity.localPath)
            if (!file.exists()) {
                documentDao.updateSyncStatus(
                    id = documentId,
                    status = SyncStatus.SYNC_FAILED,
                    error = "Local file missing",
                )
                return@withContext DocumentUploadResult.Failure("Local file missing")
            }

            val metadata = DocumentDto.UploadMetadata(
                documentType = entity.documentType.apiValue,
                fileName = entity.fileName,
                mimeType = entity.mimeType,
                entityType = entity.linkedEntityType,
                entityId = entity.linkedEntityId,
                idempotencyKey = documentId,
            )

            val metadataBody = json.encodeToString(
                DocumentDto.UploadMetadata.serializer(),
                metadata,
            ).toRequestBody("application/json".toMediaType())

            val filePart = MultipartBody.Part.createFormData(
                "file",
                entity.fileName,
                file.asRequestBody(entity.mimeType.toMediaType()),
            )

            when (
                val result = safeApiCall {
                    documentApi.uploadDocument(
                        idempotencyKey = documentId,
                        metadata = metadataBody,
                        file = filePart,
                    ).data
                }
            ) {
                is NetworkResult.Success -> {
                    val uploadedAt = System.currentTimeMillis()
                    documentDao.updateSyncStatus(
                        id = documentId,
                        status = SyncStatus.SYNCED,
                        syncedAt = uploadedAt,
                        remoteUrl = result.data.url,
                        uploadedAt = uploadedAt,
                        error = null,
                    )
                    DocumentUploadResult.Success
                }

                is NetworkResult.Error -> {
                    val retryable = result.code == null || result.code >= 500
                    documentDao.updateSyncStatus(
                        id = documentId,
                        status = if (retryable) SyncStatus.PENDING_CREATE else SyncStatus.SYNC_FAILED,
                        error = result.message,
                    )
                    if (retryable) {
                        documentUploadScheduler.scheduleUpload(documentId)
                        DocumentUploadResult.Retry
                    } else {
                        DocumentUploadResult.Failure(result.message)
                    }
                }
            }
        }

    override suspend fun syncPendingUploads(): Int =
        withContext(dispatchers.io) {
            val pending = documentDao.getPendingUpload()
            var uploaded = 0
            pending.forEach { entity ->
                when (uploadDocument(entity.id)) {
                    is DocumentUploadResult.Success -> uploaded++
                    else -> Unit
                }
            }
            uploaded
        }
}
