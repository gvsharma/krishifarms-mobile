package com.krishifarms.mobile.feature.worker.data.repository

import com.krishifarms.mobile.core.common.DispatcherProvider
import com.krishifarms.mobile.core.common.IdGenerator
import com.krishifarms.mobile.core.common.NetworkMonitor
import com.krishifarms.mobile.core.common.Resource
import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.dao.WorkOrderDao
import com.krishifarms.mobile.core.database.entity.WorkOrderEntity
import com.krishifarms.mobile.core.network.NetworkResult
import com.krishifarms.mobile.core.network.WorkOrderApiService
import com.krishifarms.mobile.core.network.dto.WorkerDtos
import com.krishifarms.mobile.core.network.safeApiCall
import com.krishifarms.mobile.feature.worker.data.mapper.buildWorkOrderEntity
import com.krishifarms.mobile.feature.worker.data.mapper.toEntity
import com.krishifarms.mobile.feature.worker.data.mapper.toWorkOrderDomain
import com.krishifarms.mobile.feature.worker.domain.model.WorkOrder
import com.krishifarms.mobile.feature.worker.domain.repository.WorkOrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkOrderRepositoryImpl @Inject constructor(
    private val workOrderDao: WorkOrderDao,
    private val workOrderApi: WorkOrderApiService,
    private val networkMonitor: NetworkMonitor,
    private val dispatchers: DispatcherProvider,
) : WorkOrderRepository {

    override fun observeWorkOrders(
        workerId: String?,
        activityType: String?,
        farmId: String?,
    ): Flow<List<WorkOrder>> =
        workOrderDao.observeFiltered(workerId, activityType, farmId)
            .map { list -> list.map { it.toWorkOrderDomain() } }

    override fun observeWorkOrder(id: String): Flow<WorkOrder?> =
        workOrderDao.observeById(id).map { it?.toWorkOrderDomain() }

    override suspend fun saveWorkOrder(
        id: String?,
        workerId: String,
        workerName: String,
        activityType: String,
        farmId: String?,
        farmName: String,
        startTime: Long,
        endTime: Long,
        hourlyRate: Double,
    ): Resource<WorkOrder> = withContext(dispatchers.io) {
        if (endTime <= startTime) {
            return@withContext Resource.Error("End time must be after start time")
        }

        val existing = id?.let { workOrderDao.getById(it) }
        val workOrderId = id ?: IdGenerator.newLocalId()
        val syncStatus = when {
            existing == null -> SyncStatus.PENDING_CREATE
            existing.sync.syncStatus == SyncStatus.PENDING_CREATE -> SyncStatus.PENDING_CREATE
            else -> SyncStatus.PENDING_UPDATE
        }

        val entity = buildWorkOrderEntity(
            id = workOrderId,
            workerId = workerId,
            workerName = workerName,
            activityType = activityType,
            farmId = farmId,
            farmName = farmName,
            startTime = startTime,
            endTime = endTime,
            hourlyRate = hourlyRate,
            syncStatus = syncStatus,
            existingSync = existing?.sync,
        )

        workOrderDao.upsert(entity)

        if (networkMonitor.isOnline()) {
            syncSingleWorkOrder(entity)
        } else {
            Resource.Success(entity.toWorkOrderDomain())
        }
    }

    override suspend fun syncWorkOrders(): Resource<Unit> = withContext(dispatchers.io) {
        if (!networkMonitor.isOnline()) {
            return@withContext Resource.Error("You are offline")
        }

        workOrderDao.getPendingSync().forEach { syncSingleWorkOrder(it) }

        when (val pullResult = safeApiCall { workOrderApi.getWorkOrders() }) {
            is NetworkResult.Success -> {
                workOrderDao.upsertAll(pullResult.data.items.map { it.toEntity() })
                Resource.Success(Unit)
            }
            is NetworkResult.Error -> Resource.Error(pullResult.message)
        }
    }

    private suspend fun syncSingleWorkOrder(entity: WorkOrderEntity): Resource<WorkOrder> {
        if (entity.sync.syncStatus != SyncStatus.PENDING_CREATE &&
            entity.sync.syncStatus != SyncStatus.PENDING_UPDATE
        ) {
            return Resource.Success(entity.toWorkOrderDomain())
        }

        val request = WorkerDtos.CreateWorkOrderRequest(
            workerId = entity.workerId,
            activityType = entity.activityType,
            farmId = entity.farmId,
            farmName = entity.farmName,
            startTime = entity.startTime,
            endTime = entity.endTime,
            hourlyRate = entity.hourlyRate,
        )

        return when (val result = safeApiCall { workOrderApi.createWorkOrder(request) }) {
            is NetworkResult.Success -> {
                val synced = result.data.toEntity(localId = entity.id)
                workOrderDao.upsert(synced)
                Resource.Success(synced.toWorkOrderDomain())
            }
            is NetworkResult.Error -> {
                workOrderDao.upsert(
                    entity.copy(
                        sync = entity.sync.copy(
                            syncStatus = SyncStatus.SYNC_FAILED,
                            syncError = result.message,
                        ),
                    ),
                )
                Resource.Error(result.message)
            }
        }
    }
}
