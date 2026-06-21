package com.krishifarms.mobile.feature.procurement.data.repository

import com.krishifarms.mobile.core.common.DispatcherProvider
import com.krishifarms.mobile.core.common.IdGenerator
import com.krishifarms.mobile.core.common.NetworkMonitor
import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.dao.FarmerDao
import com.krishifarms.mobile.core.database.dao.ProcurementDao
import com.krishifarms.mobile.core.database.entity.ProcurementEntity
import com.krishifarms.mobile.core.database.entity.SyncMetadata
import com.krishifarms.mobile.core.network.safeApiCall
import com.krishifarms.mobile.core.sync.OfflineSyncEngine
import com.krishifarms.mobile.core.sync.domain.SyncEntityType
import com.krishifarms.mobile.core.sync.domain.SyncOperationType
import com.krishifarms.mobile.feature.procurement.data.mapper.toDomain
import com.krishifarms.mobile.feature.procurement.data.mapper.toEntity
import com.krishifarms.mobile.feature.procurement.data.remote.ProcurementApi
import com.krishifarms.mobile.feature.procurement.data.remote.dto.CreateProcurementRequest
import com.krishifarms.mobile.feature.procurement.domain.model.FarmerOption
import com.krishifarms.mobile.feature.procurement.domain.model.Procurement
import com.krishifarms.mobile.feature.procurement.domain.repository.CreateProcurementInput
import com.krishifarms.mobile.feature.procurement.domain.repository.ProcurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcurementRepositoryImpl @Inject constructor(
    private val procurementDao: ProcurementDao,
    private val farmerDao: FarmerDao,
    private val procurementApi: ProcurementApi,
    private val networkMonitor: NetworkMonitor,
    private val dispatchers: DispatcherProvider,
    private val offlineSyncEngine: OfflineSyncEngine,
    private val json: Json,
) : ProcurementRepository {

    override fun observeProcurements(): Flow<List<Procurement>> =
        procurementDao.observeAllWithFarmer().map { list -> list.map { it.toDomain() } }

    override fun observeProcurement(id: String): Flow<Procurement?> =
        procurementDao.observeById(id).map { entity ->
            entity?.let { procurement ->
                val farmer = farmerDao.getById(procurement.farmerId)
                procurement.toDomain(
                    farmerName = farmer?.name,
                    farmerVillage = farmer?.village,
                )
            }
        }

    override fun observeFarmerOptions(): Flow<List<FarmerOption>> =
        farmerDao.observeAll().map { farmers ->
            farmers.map { FarmerOption(id = it.id, name = it.name, village = it.village) }
        }

    override suspend fun createProcurement(input: CreateProcurementInput): Result<String> =
        withContext(dispatchers.io) {
            runCatching {
                val localId = IdGenerator.newLocalId()
                val now = System.currentTimeMillis()
                val entity = ProcurementEntity(
                    id = localId,
                    serverId = null,
                    farmerId = input.farmerId,
                    crop = input.crop,
                    village = input.village,
                    bags = input.bags,
                    weight = input.weight,
                    moisture = input.moisture,
                    rate = input.rate,
                    deductions = input.deductions,
                    netAmount = input.netAmount,
                    localImagePath = input.localImagePath,
                    localBillPath = input.localBillPath,
                    remoteImageUrl = null,
                    remoteBillUrl = null,
                    createdAt = now,
                    sync = SyncMetadata(
                        syncStatus = SyncStatus.PENDING_CREATE,
                        localUpdatedAt = now,
                    ),
                )
                procurementDao.upsert(entity)

                val farmerServerId = farmerDao.getById(input.farmerId)?.serverId ?: input.farmerId
                val payload = CreateProcurementRequest(
                    localId = localId,
                    farmerId = farmerServerId,
                    crop = input.crop,
                    village = input.village,
                    bags = input.bags,
                    weight = input.weight,
                    moisture = input.moisture,
                    rate = input.rate,
                    deductions = input.deductions,
                    netAmount = input.netAmount,
                )

                offlineSyncEngine.enqueue(
                    entityType = SyncEntityType.PROCUREMENT,
                    entityId = localId,
                    operationType = SyncOperationType.CREATE,
                    payloadJson = json.encodeToString(CreateProcurementRequest.serializer(), payload),
                    idempotencyKey = localId,
                    priority = 1,
                )
                localId
            }
        }

    override suspend fun refreshFromServer() = withContext(dispatchers.io) {
        if (!networkMonitor.isOnline()) return@withContext
        var page = 1
        var totalPages = 1
        while (page <= totalPages) {
            when (val result = safeApiCall { procurementApi.getProcurements(page = page, pageSize = PAGE_SIZE) }) {
                is com.krishifarms.mobile.core.network.NetworkResult.Success -> {
                    val payload = result.data.data
                    totalPages = payload.totalPages
                    payload.items.forEach { dto ->
                        val existing = procurementDao.getById(dto.id)
                            ?: dto.id.let { procurementDao.getById("local_$it") }
                        procurementDao.upsert(dto.toEntity(existing))
                    }
                    page++
                }
                is com.krishifarms.mobile.core.network.NetworkResult.Error -> break
            }
        }
    }

    override suspend fun syncPending() {
        withContext(dispatchers.io) {
            offlineSyncEngine.processQueue()
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
