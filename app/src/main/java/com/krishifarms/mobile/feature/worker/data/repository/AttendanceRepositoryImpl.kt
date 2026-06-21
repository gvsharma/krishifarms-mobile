package com.krishifarms.mobile.feature.worker.data.repository

import com.krishifarms.mobile.core.common.DispatcherProvider
import com.krishifarms.mobile.core.common.IdGenerator
import com.krishifarms.mobile.core.common.NetworkMonitor
import com.krishifarms.mobile.core.common.Resource
import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.dao.AttendanceDao
import com.krishifarms.mobile.core.database.dao.WorkerDao
import com.krishifarms.mobile.core.database.entity.AttendanceEntity
import com.krishifarms.mobile.core.database.entity.AttendanceStatus
import com.krishifarms.mobile.core.database.entity.SyncMetadata
import com.krishifarms.mobile.core.network.AttendanceApiService
import com.krishifarms.mobile.core.network.NetworkResult
import com.krishifarms.mobile.core.network.dto.WorkerDtos
import com.krishifarms.mobile.core.network.safeApiCall
import com.krishifarms.mobile.feature.worker.data.mapper.toDomain
import com.krishifarms.mobile.feature.worker.data.mapper.toEntity
import com.krishifarms.mobile.feature.worker.domain.model.Attendance
import com.krishifarms.mobile.feature.worker.domain.model.DateUtils
import com.krishifarms.mobile.feature.worker.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val workerDao: WorkerDao,
    private val attendanceApi: AttendanceApiService,
    private val networkMonitor: NetworkMonitor,
    private val dispatchers: DispatcherProvider,
) : AttendanceRepository {

    override fun observeAttendanceForDate(date: Long): Flow<List<Attendance>> {
        val dayStart = DateUtils.startOfDay(date)
        return combine(
            workerDao.observeAll(),
            attendanceDao.observeByDate(dayStart),
        ) { workers, records ->
            val recordMap = records.associateBy { it.workerId }
            workers.map { worker ->
                val record = recordMap[worker.id]
                if (record != null) {
                    record.toDomain(worker.name)
                } else {
                    Attendance(
                        id = "",
                        workerId = worker.id,
                        workerName = worker.name,
                        date = dayStart,
                        checkIn = null,
                        checkOut = null,
                        status = AttendanceStatus.ABSENT,
                        syncStatus = SyncStatus.SYNCED,
                    )
                }
            }
        }
    }

    override fun observeAttendanceForWorker(workerId: String): Flow<List<Attendance>> =
        combine(
            workerDao.observeById(workerId),
            attendanceDao.observeByWorker(workerId),
        ) { worker, records ->
            records.map { it.toDomain(worker?.name.orEmpty()) }
        }

    override suspend fun markAttendance(
        workerId: String,
        date: Long,
        status: AttendanceStatus,
        checkIn: Long?,
        checkOut: Long?,
    ): Resource<Attendance> = withContext(dispatchers.io) {
        val dayStart = DateUtils.startOfDay(date)
        val existing = attendanceDao.getByWorkerAndDate(workerId, dayStart)
        val attendanceId = existing?.id ?: IdGenerator.newLocalId()
        val syncStatus = when {
            existing == null -> SyncStatus.PENDING_CREATE
            existing.sync.syncStatus == SyncStatus.PENDING_CREATE -> SyncStatus.PENDING_CREATE
            else -> SyncStatus.PENDING_UPDATE
        }

        val entity = AttendanceEntity(
            id = attendanceId,
            workerId = workerId,
            date = dayStart,
            checkIn = checkIn,
            checkOut = checkOut,
            status = status,
            sync = (existing?.sync ?: SyncMetadata()).copy(
                syncStatus = syncStatus,
                localUpdatedAt = System.currentTimeMillis(),
            ),
        )

        attendanceDao.upsert(entity)
        val workerName = workerDao.getById(workerId)?.name.orEmpty()

        if (networkMonitor.isOnline()) {
            syncSingleAttendance(entity, workerName)
        } else {
            Resource.Success(entity.toDomain(workerName))
        }
    }

    override suspend fun syncAttendance(): Resource<Unit> = withContext(dispatchers.io) {
        if (!networkMonitor.isOnline()) {
            return@withContext Resource.Error("You are offline")
        }

        attendanceDao.getPendingSync().forEach { record ->
            val workerName = workerDao.getById(record.workerId)?.name.orEmpty()
            syncSingleAttendance(record, workerName)
        }

        when (val pullResult = safeApiCall { attendanceApi.getAttendance() }) {
            is NetworkResult.Success -> {
                attendanceDao.upsertAll(pullResult.data.items.map { it.toEntity() })
                Resource.Success(Unit)
            }
            is NetworkResult.Error -> Resource.Error(pullResult.message)
        }
    }

    private suspend fun syncSingleAttendance(
        entity: AttendanceEntity,
        workerName: String,
    ): Resource<Attendance> {
        val request = WorkerDtos.UpsertAttendanceRequest(
            workerId = entity.workerId,
            date = entity.date,
            checkIn = entity.checkIn,
            checkOut = entity.checkOut,
            status = entity.status.name,
        )

        return when (val result = safeApiCall { attendanceApi.upsertAttendance(request) }) {
            is NetworkResult.Success -> {
                val synced = result.data.toEntity(localId = entity.id)
                attendanceDao.upsert(synced)
                Resource.Success(synced.toDomain(workerName))
            }
            is NetworkResult.Error -> {
                attendanceDao.upsert(
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
