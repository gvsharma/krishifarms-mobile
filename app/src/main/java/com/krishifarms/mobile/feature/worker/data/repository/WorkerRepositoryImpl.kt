package com.krishifarms.mobile.feature.worker.data.repository

import com.krishifarms.mobile.core.common.DispatcherProvider
import com.krishifarms.mobile.core.common.IdGenerator
import com.krishifarms.mobile.core.common.Resource
import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.dao.WorkerDao
import com.krishifarms.mobile.core.database.entity.SyncMetadata
import com.krishifarms.mobile.core.network.NetworkResult
import com.krishifarms.mobile.core.network.WorkerApiService
import com.krishifarms.mobile.core.network.dto.WorkerDtos
import com.krishifarms.mobile.core.network.safeApiCall
import com.krishifarms.mobile.core.sync.OfflineSyncEngine
import com.krishifarms.mobile.core.sync.domain.SyncEntityType
import com.krishifarms.mobile.core.sync.domain.SyncOperationType
import com.krishifarms.mobile.feature.worker.data.mapper.toDomain
import com.krishifarms.mobile.feature.worker.data.mapper.toEntity
import com.krishifarms.mobile.feature.worker.domain.model.Worker
import com.krishifarms.mobile.feature.worker.domain.repository.WorkerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkerRepositoryImpl @Inject constructor(
    private val workerDao: WorkerDao,
    private val workerApi: WorkerApiService,
    private val dispatchers: DispatcherProvider,
    private val offlineSyncEngine: OfflineSyncEngine,
    private val json: Json,
) : WorkerRepository {

    override fun observeWorkers(searchQuery: String): Flow<List<Worker>> {
        val source = if (searchQuery.isBlank()) {
            workerDao.observeAll()
        } else {
            workerDao.search(searchQuery.trim())
        }
        return source.map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeWorker(id: String): Flow<Worker?> =
        workerDao.observeById(id).map { it?.toDomain() }

    override suspend fun getWorker(id: String): Worker? =
        withContext(dispatchers.io) { workerDao.getById(id)?.toDomain() }

    override suspend fun saveWorker(
        id: String?,
        name: String,
        phone: String?,
        hourlyRate: Double,
    ): Resource<Worker> = withContext(dispatchers.io) {
        val existing = id?.let { workerDao.getById(it) }
        val workerId = id ?: IdGenerator.newLocalId()
        val isCreate = existing == null
        val syncStatus = when {
            isCreate -> SyncStatus.PENDING_CREATE
            existing.sync.syncStatus == SyncStatus.PENDING_CREATE -> SyncStatus.PENDING_CREATE
            else -> SyncStatus.PENDING_UPDATE
        }

        val entity = WorkerEntity(
            id = workerId,
            name = name.trim(),
            phone = phone?.trim()?.ifBlank { null },
            defaultHourlyRate = hourlyRate,
            active = existing?.active ?: true,
            sync = (existing?.sync ?: SyncMetadata()).copy(
                syncStatus = syncStatus,
                localUpdatedAt = System.currentTimeMillis(),
            ),
        )

        workerDao.upsert(entity)

        val payload = if (isCreate) {
            WorkerDtos.CreateWorkerRequest(
                name = entity.name,
                phone = entity.phone,
                defaultHourlyRate = entity.defaultHourlyRate,
            )
        } else {
            WorkerDtos.UpdateWorkerRequest(
                name = entity.name,
                phone = entity.phone,
                defaultHourlyRate = entity.defaultHourlyRate,
                active = entity.active,
            )
        }

        offlineSyncEngine.enqueue(
            entityType = SyncEntityType.WORKER,
            entityId = workerId,
            operationType = if (isCreate) SyncOperationType.CREATE else SyncOperationType.UPDATE,
            payloadJson = if (isCreate) {
                json.encodeToString(WorkerDtos.CreateWorkerRequest.serializer(), payload as WorkerDtos.CreateWorkerRequest)
            } else {
                json.encodeToString(WorkerDtos.UpdateWorkerRequest.serializer(), payload as WorkerDtos.UpdateWorkerRequest)
            },
            idempotencyKey = workerId,
        )

        Resource.Success(entity.toDomain())
    }

    override suspend fun syncWorkers(): Resource<Unit> = withContext(dispatchers.io) {
        offlineSyncEngine.processQueue()
        when (val pullResult = safeApiCall { workerApi.getWorkers() }) {
            is NetworkResult.Success -> {
                workerDao.upsertAll(pullResult.data.items.map { it.toEntity() })
                Resource.Success(Unit)
            }
            is NetworkResult.Error -> Resource.Error(pullResult.message)
        }
    }
}

private typealias WorkerEntity = com.krishifarms.mobile.core.database.entity.WorkerEntity
