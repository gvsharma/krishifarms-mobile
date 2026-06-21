package com.krishifarms.mobile.core.sync.handler

import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.dao.FarmerDao
import com.krishifarms.mobile.core.database.entity.SyncOperationEntity
import com.krishifarms.mobile.core.data.mapper.toEntity
import com.krishifarms.mobile.core.network.FarmerApiService
import com.krishifarms.mobile.core.network.dto.FarmerDtos
import com.krishifarms.mobile.core.sync.domain.RemoteEntitySnapshot
import com.krishifarms.mobile.core.sync.domain.SyncEntityType
import com.krishifarms.mobile.core.sync.domain.SyncHandler
import com.krishifarms.mobile.core.sync.domain.SyncHandlerResult
import com.krishifarms.mobile.core.sync.domain.SyncOperationType
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmerSyncHandler @Inject constructor(
    private val farmerApi: FarmerApiService,
    private val farmerDao: FarmerDao,
    json: Json,
) : SyncHandler, BaseSyncHandler(json) {

    override val entityType: SyncEntityType = SyncEntityType.FARMER

    override suspend fun execute(operation: SyncOperationEntity): SyncHandlerResult {
        val payload = json.decodeFromString<FarmerSyncPayload>(operation.payloadJson)
        val serverId = operation.entityId.removePrefix("local_").let { stripped ->
            if (stripped == operation.entityId && operation.entityId.startsWith("local_")) {
                null
            } else {
                operation.entityId.takeIf { !it.startsWith("local_") }
            }
        }

        return when (operation.operationType) {
            SyncOperationType.CREATE -> executeApi {
                val response = farmerApi.createFarmer(
                    idempotencyKey = operation.idempotencyKey,
                    request = FarmerDtos.CreateFarmerRequest(
                        name = payload.name,
                        phone = payload.phone,
                        village = payload.village,
                        bankDetails = payload.bankDetails,
                        landAcres = payload.landAcres,
                        cropTypes = payload.cropTypes,
                    ),
                )
                farmerDao.upsert(response.toEntity(localId = operation.entityId))
                response
            }

            SyncOperationType.UPDATE -> executeApi {
                val targetId = serverId ?: farmerDao.getById(operation.entityId)?.serverId ?: operation.entityId
                val response = farmerApi.updateFarmer(
                    id = targetId,
                    idempotencyKey = operation.idempotencyKey,
                    request = FarmerDtos.UpdateFarmerRequest(
                        name = payload.name,
                        phone = payload.phone,
                        village = payload.village,
                        bankDetails = payload.bankDetails,
                        landAcres = payload.landAcres,
                        cropTypes = payload.cropTypes,
                    ),
                )
                farmerDao.upsert(response.toEntity(localId = operation.entityId))
                response
            }

            SyncOperationType.DELETE -> {
                farmerDao.softDelete(operation.entityId, SyncStatus.SYNCED)
                SyncHandlerResult.Success()
            }
        }
    }

    override suspend fun fetchRemote(entityId: String): RemoteEntitySnapshot? {
        return runCatching {
            val remoteId = farmerDao.getById(entityId)?.serverId ?: entityId.removePrefix("local_")
            val dto = farmerApi.getFarmer(remoteId)
            RemoteEntitySnapshot(
                entityId = dto.id,
                updatedAt = dto.updatedAt,
                payloadJson = json.encodeToString(FarmerDtos.FarmerDto.serializer(), dto),
            )
        }.getOrNull()
    }
}
