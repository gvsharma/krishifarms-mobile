package com.krishifarms.mobile.core.sync.handler

import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.dao.ProcurementDao
import com.krishifarms.mobile.core.database.entity.SyncOperationEntity
import com.krishifarms.mobile.core.network.safeApiCall
import com.krishifarms.mobile.feature.procurement.data.mapper.toEntity
import com.krishifarms.mobile.feature.procurement.data.remote.ProcurementApi
import com.krishifarms.mobile.feature.procurement.data.remote.dto.CreateProcurementRequest
import com.krishifarms.mobile.core.sync.domain.RemoteEntitySnapshot
import com.krishifarms.mobile.core.sync.domain.SyncEntityType
import com.krishifarms.mobile.core.sync.domain.SyncHandler
import com.krishifarms.mobile.core.sync.domain.SyncHandlerResult
import com.krishifarms.mobile.core.sync.domain.SyncOperationType
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcurementSyncHandler @Inject constructor(
    private val procurementApi: ProcurementApi,
    private val procurementDao: ProcurementDao,
    json: Json,
) : SyncHandler, BaseSyncHandler(json) {

    override val entityType: SyncEntityType = SyncEntityType.PROCUREMENT

    override suspend fun execute(operation: SyncOperationEntity): SyncHandlerResult {
        if (operation.operationType != SyncOperationType.CREATE) {
            return SyncHandlerResult.PermanentFailure("Only procurement create is supported")
        }

        val payload = json.decodeFromString<CreateProcurementRequest>(operation.payloadJson)
        return when (val result = safeApiCall { procurementApi.createProcurement(payload) }) {
            is com.krishifarms.mobile.core.network.NetworkResult.Success -> {
                val created = result.data.data.procurement
                val local = procurementDao.getById(operation.entityId)
                var updated = created.toEntity(local).copy(id = operation.entityId, serverId = created.id)
                updated = uploadAttachments(updated, created.id)
                procurementDao.upsert(updated)
                SyncHandlerResult.Success(serverEntityId = created.id)
            }

            is com.krishifarms.mobile.core.network.NetworkResult.Error -> {
                if (result.code == 409) {
                    SyncHandlerResult.Conflict(
                        serverSnapshot = RemoteEntitySnapshot(
                            entityId = operation.entityId,
                            updatedAt = System.currentTimeMillis(),
                            payloadJson = operation.payloadJson,
                        ),
                        clientUpdatedAt = System.currentTimeMillis(),
                    )
                } else if (result.code != null && result.code >= 500) {
                    SyncHandlerResult.RetryableFailure(result.message)
                } else {
                    procurementDao.update(
                        procurementDao.getById(operation.entityId)?.copy(
                            sync = procurementDao.getById(operation.entityId)!!.sync.copy(
                                syncStatus = SyncStatus.SYNC_FAILED,
                                syncError = result.message,
                            ),
                        ) ?: return SyncHandlerResult.PermanentFailure(result.message),
                    )
                    SyncHandlerResult.PermanentFailure(result.message)
                }
            }
        }
    }

    override suspend fun fetchRemote(entityId: String): RemoteEntitySnapshot? = null

    private suspend fun uploadAttachments(
        entity: com.krishifarms.mobile.core.database.entity.ProcurementEntity,
        serverId: String,
    ): com.krishifarms.mobile.core.database.entity.ProcurementEntity {
        var current = entity
        entity.localImagePath?.let { path ->
            val file = java.io.File(path)
            if (file.exists()) {
                val part = MultipartBody.Part.createFormData(
                    "image",
                    file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull()),
                )
                when (val result = safeApiCall { procurementApi.uploadImage(serverId, part) }) {
                    is com.krishifarms.mobile.core.network.NetworkResult.Success -> {
                        current = current.copy(remoteImageUrl = result.data.data.url)
                    }
                    else -> Unit
                }
            }
        }
        entity.localBillPath?.let { path ->
            val file = java.io.File(path)
            if (file.exists()) {
                val part = MultipartBody.Part.createFormData(
                    "bill",
                    file.name,
                    file.asRequestBody("application/octet-stream".toMediaTypeOrNull()),
                )
                when (val result = safeApiCall { procurementApi.uploadBill(serverId, part) }) {
                    is com.krishifarms.mobile.core.network.NetworkResult.Success -> {
                        current = current.copy(remoteBillUrl = result.data.data.url)
                    }
                    else -> Unit
                }
            }
        }
        return current.copy(
            sync = current.sync.copy(
                syncStatus = SyncStatus.SYNCED,
                lastSyncedAt = System.currentTimeMillis(),
            ),
        )
    }
}
