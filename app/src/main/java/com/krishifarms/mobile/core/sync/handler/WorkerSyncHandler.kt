package com.krishifarms.mobile.core.sync.handler

import com.krishifarms.mobile.core.database.dao.WorkerDao
import com.krishifarms.mobile.core.database.entity.SyncOperationEntity
import com.krishifarms.mobile.core.network.WorkerApiService
import com.krishifarms.mobile.core.network.dto.WorkerDtos
import com.krishifarms.mobile.core.sync.domain.RemoteEntitySnapshot
import com.krishifarms.mobile.core.sync.domain.SyncEntityType
import com.krishifarms.mobile.core.sync.domain.SyncHandler
import com.krishifarms.mobile.core.sync.domain.SyncHandlerResult
import com.krishifarms.mobile.core.sync.domain.SyncOperationType
import com.krishifarms.mobile.feature.worker.data.mapper.toEntity
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkerSyncHandler @Inject constructor(
    private val workerApi: WorkerApiService,
    private val workerDao: WorkerDao,
    json: Json,
) : SyncHandler, BaseSyncHandler(json) {

    override val entityType: SyncEntityType = SyncEntityType.WORKER

    override suspend fun execute(operation: SyncOperationEntity): SyncHandlerResult {
        return when (operation.operationType) {
            SyncOperationType.CREATE -> {
                val payload = json.decodeFromString<WorkerDtos.CreateWorkerRequest>(operation.payloadJson)
                executeApi {
                    val response = workerApi.createWorker(operation.idempotencyKey, payload)
                    workerDao.upsert(response.toEntity(localId = operation.entityId))
                    response
                }.let { if (it is SyncHandlerResult.Success) SyncHandlerResult.Success() else it }
            }

            SyncOperationType.UPDATE -> {
                val payload = json.decodeFromString<WorkerDtos.UpdateWorkerRequest>(operation.payloadJson)
                val targetId = operation.entityId.removePrefix("local_").let {
                    if (operation.entityId.startsWith("local_")) operation.entityId else operation.entityId
                }
                executeApi {
                    val response = workerApi.updateWorker(
                        id = if (operation.entityId.startsWith("local_")) operation.entityId else targetId,
                        idempotencyKey = operation.idempotencyKey,
                        request = payload,
                    )
                    workerDao.upsert(response.toEntity(localId = operation.entityId))
                    response
                }
            }

            SyncOperationType.DELETE -> SyncHandlerResult.Success()
        }
    }

    override suspend fun fetchRemote(entityId: String): RemoteEntitySnapshot? {
        return runCatching {
            val remoteId = if (entityId.startsWith("local_")) entityId else entityId
            val dto = workerApi.getWorker(remoteId)
            RemoteEntitySnapshot(
                entityId = dto.id,
                updatedAt = dto.updatedAt ?: System.currentTimeMillis(),
                payloadJson = json.encodeToString(WorkerDtos.WorkerDto.serializer(), dto),
            )
        }.getOrNull()
    }

    override fun mapHttpException(exception: HttpException): SyncHandlerResult {
        if (exception.code() == 409) {
            return SyncHandlerResult.Conflict(
                serverSnapshot = RemoteEntitySnapshot("", 0L, "{}"),
                clientUpdatedAt = System.currentTimeMillis(),
            )
        }
        return super.mapHttpException(exception)
    }
}
