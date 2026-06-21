package com.krishifarms.mobile.core.sync.handler

import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.dao.DocumentDao
import com.krishifarms.mobile.core.database.entity.SyncOperationEntity
import com.krishifarms.mobile.core.network.DocumentApiService
import com.krishifarms.mobile.core.network.dto.DocumentDtos
import com.krishifarms.mobile.core.sync.domain.RemoteEntitySnapshot
import com.krishifarms.mobile.core.sync.domain.SyncEntityType
import com.krishifarms.mobile.core.sync.domain.SyncHandler
import com.krishifarms.mobile.core.sync.domain.SyncHandlerResult
import com.krishifarms.mobile.core.sync.domain.SyncOperationType
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentSyncHandler @Inject constructor(
    private val documentApi: DocumentApiService,
    private val documentDao: DocumentDao,
    json: Json,
) : SyncHandler, BaseSyncHandler(json) {

    override val entityType: SyncEntityType = SyncEntityType.DOCUMENT

    override suspend fun execute(operation: SyncOperationEntity): SyncHandlerResult {
        if (operation.operationType != SyncOperationType.CREATE) {
            return SyncHandlerResult.PermanentFailure("Only document upload (CREATE) is supported")
        }

        val metadata = json.decodeFromString<DocumentDtos.UploadDocumentRequest>(operation.payloadJson)
        val localDocument = documentDao.getById(operation.entityId)
            ?: return SyncHandlerResult.PermanentFailure("Local document not found")

        val file = File(localDocument.localPath)
        if (!file.exists()) {
            return SyncHandlerResult.PermanentFailure("Local file missing: ${localDocument.localPath}")
        }

        return executeApi {
            val metadataBody = json.encodeToString(
                DocumentDtos.UploadDocumentRequest.serializer(),
                metadata,
            ).toRequestBody("application/json".toMediaType())

            val filePart = MultipartBody.Part.createFormData(
                "file",
                localDocument.fileName,
                file.asRequestBody(localDocument.mimeType.toMediaType()),
            )

            val response = documentApi.uploadDocument(
                idempotencyKey = operation.idempotencyKey,
                metadata = metadataBody,
                file = filePart,
            )

            documentDao.updateSyncStatus(
                id = operation.entityId,
                status = SyncStatus.SYNCED,
                syncedAt = System.currentTimeMillis(),
                remoteUrl = response.document.url,
                uploadedAt = response.document.uploadedAt,
            )
            response
        }.let { if (it is SyncHandlerResult.Success) SyncHandlerResult.Success() else it }
    }

    override suspend fun fetchRemote(entityId: String): RemoteEntitySnapshot? {
        return runCatching {
            val dto = documentApi.getDocument(entityId)
            RemoteEntitySnapshot(
                entityId = dto.id,
                updatedAt = dto.updatedAt,
                payloadJson = json.encodeToString(DocumentDtos.DocumentDto.serializer(), dto),
            )
        }.getOrNull()
    }
}
