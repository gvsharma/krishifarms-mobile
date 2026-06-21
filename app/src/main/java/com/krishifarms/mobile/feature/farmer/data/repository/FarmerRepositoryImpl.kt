package com.krishifarms.mobile.feature.farmer.data.repository

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.krishifarms.mobile.core.common.DispatcherProvider
import com.krishifarms.mobile.core.common.IdGenerator
import com.krishifarms.mobile.core.common.NetworkMonitor
import com.krishifarms.mobile.core.common.Resource
import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.dao.FarmerDao
import com.krishifarms.mobile.core.network.FarmerApiService
import com.krishifarms.mobile.core.network.NetworkResult
import com.krishifarms.mobile.core.network.dto.FarmerDtos
import com.krishifarms.mobile.core.network.safeApiCall
import com.krishifarms.mobile.feature.farmer.data.mapper.toDomain
import com.krishifarms.mobile.feature.farmer.data.mapper.toEntity
import com.krishifarms.mobile.feature.farmer.domain.model.Farmer
import com.krishifarms.mobile.feature.farmer.domain.repository.FarmerInput
import com.krishifarms.mobile.feature.farmer.domain.repository.FarmerRepository
import com.krishifarms.mobile.feature.farmer.sync.FarmerSyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmerRepositoryImpl @Inject constructor(
    private val farmerDao: FarmerDao,
    private val farmerApi: FarmerApiService,
    private val networkMonitor: NetworkMonitor,
    private val dispatchers: DispatcherProvider,
    private val workManager: WorkManager,
) : FarmerRepository {

    override fun getFarmers(searchQuery: String): Flow<List<Farmer>> {
        val source = if (searchQuery.isBlank()) {
            farmerDao.observeAll()
        } else {
            farmerDao.search(searchQuery.trim())
        }
        return source.map { entities -> entities.map { it.toDomain() } }
    }

    override fun getFarmerById(id: String): Flow<Farmer?> =
        farmerDao.observeById(id).map { it?.toDomain() }

    override suspend fun createFarmer(input: FarmerInput): Resource<Farmer> =
        withContext(dispatchers.io) {
            val localId = IdGenerator.newLocalId()
            val farmer = Farmer(
                id = localId,
                serverId = null,
                name = input.name.trim(),
                village = input.village.trim(),
                phone = input.phone.trim(),
                bankDetails = input.bankDetails.trim(),
                landAcres = input.landAcres,
                cropTypes = input.cropTypes,
                syncStatus = SyncStatus.PENDING_CREATE,
            )
            farmerDao.upsert(farmer.toEntity(syncStatus = SyncStatus.PENDING_CREATE))
            scheduleSync()
            if (networkMonitor.isOnline()) {
                syncSingleFarmer(farmerDao.getById(localId)!!)
            } else {
                Resource.Success(farmer)
            }
        }

    override suspend fun updateFarmer(id: String, input: FarmerInput): Resource<Farmer> =
        withContext(dispatchers.io) {
            val existing = farmerDao.getById(id)
                ?: return@withContext Resource.Error("Farmer not found")

            val syncStatus = when (existing.sync.syncStatus) {
                SyncStatus.PENDING_CREATE -> SyncStatus.PENDING_CREATE
                else -> SyncStatus.PENDING_UPDATE
            }

            val updated = Farmer(
                id = existing.id,
                serverId = existing.serverId,
                name = input.name.trim(),
                village = input.village.trim(),
                phone = input.phone.trim(),
                bankDetails = input.bankDetails.trim(),
                landAcres = input.landAcres,
                cropTypes = input.cropTypes,
                syncStatus = syncStatus,
            )
            farmerDao.upsert(updated.toEntity(syncStatus = syncStatus, existingSync = existing.sync))
            scheduleSync()
            if (networkMonitor.isOnline()) {
                syncSingleFarmer(farmerDao.getById(id)!!)
            } else {
                Resource.Success(updated)
            }
        }

    override suspend fun syncFarmers(): Resource<Unit> = withContext(dispatchers.io) {
        if (!networkMonitor.isOnline()) {
            return@withContext Resource.Error("You are offline")
        }

        farmerDao.getPendingSync().forEach { farmer ->
            syncSingleFarmer(farmer)
        }

        var page = 1
        var totalPages = 1
        while (page <= totalPages) {
            when (val result = safeApiCall { farmerApi.getFarmers(page = page, pageSize = PAGE_SIZE) }) {
                is NetworkResult.Success -> {
                    val payload = result.data
                    totalPages = payload.totalPages
                    payload.items.forEach { dto ->
                        val existing = farmerDao.getById(dto.id)
                            ?: farmerDao.getById("local_${dto.id}")
                        farmerDao.upsert(dto.toEntity(localId = existing?.id))
                    }
                    page++
                }
                is NetworkResult.Error -> return@withContext Resource.Error(result.message)
            }
        }
        Resource.Success(Unit)
    }

    private suspend fun syncSingleFarmer(entity: com.krishifarms.mobile.core.database.entity.FarmerEntity): Resource<Farmer> {
        val request = FarmerDtos.CreateFarmerRequest(
            name = entity.name,
            phone = entity.phone,
            village = entity.village,
            bankDetails = entity.bankDetails,
            landAcres = entity.landAcres,
            cropTypes = entity.cropTypes.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        )

        return when (entity.sync.syncStatus) {
            SyncStatus.PENDING_CREATE -> {
                when (
                    val result = safeApiCall {
                        farmerApi.createFarmer(
                            idempotencyKey = entity.id,
                            request = request,
                        )
                    }
                ) {
                    is NetworkResult.Success -> {
                        val synced = result.data.toEntity(localId = entity.id)
                        farmerDao.upsert(synced)
                        Resource.Success(synced.toDomain())
                    }
                    is NetworkResult.Error -> {
                        farmerDao.update(
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

            SyncStatus.PENDING_UPDATE, SyncStatus.SYNC_FAILED -> {
                val serverId = entity.serverId ?: entity.id.removePrefix("local_")
                if (entity.id.startsWith("local_") && entity.serverId == null) {
                    return syncSingleFarmer(
                        entity.copy(sync = entity.sync.copy(syncStatus = SyncStatus.PENDING_CREATE)),
                    )
                }
                when (
                    val result = safeApiCall {
                        farmerApi.updateFarmer(
                            id = serverId,
                            idempotencyKey = UUID.randomUUID().toString(),
                            request = FarmerDtos.UpdateFarmerRequest(
                                name = entity.name,
                                phone = entity.phone,
                                village = entity.village,
                                bankDetails = entity.bankDetails,
                                landAcres = entity.landAcres,
                                cropTypes = request.cropTypes,
                            ),
                        )
                    }
                ) {
                    is NetworkResult.Success -> {
                        val synced = result.data.toEntity(localId = entity.id)
                        farmerDao.upsert(synced)
                        Resource.Success(synced.toDomain())
                    }
                    is NetworkResult.Error -> {
                        farmerDao.update(
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

            else -> Resource.Success(entity.toDomain())
        }
    }

    private fun scheduleSync() {
        val request = OneTimeWorkRequestBuilder<FarmerSyncWorker>().build()
        workManager.enqueueUniqueWork(
            FarmerSyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    companion object {
        private const val PAGE_SIZE = 50
    }
}
