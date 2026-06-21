package com.krishifarms.mobile.feature.worker.data.mapper

import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.entity.AttendanceEntity
import com.krishifarms.mobile.core.database.entity.AttendanceStatus
import com.krishifarms.mobile.core.database.entity.SyncMetadata
import com.krishifarms.mobile.core.database.entity.WorkOrderEntity
import com.krishifarms.mobile.core.database.entity.WorkerEntity
import com.krishifarms.mobile.core.network.dto.WorkerDtos
import com.krishifarms.mobile.feature.worker.domain.model.Attendance
import com.krishifarms.mobile.feature.worker.domain.model.WorkOrder
import com.krishifarms.mobile.feature.worker.domain.model.WorkOrderCalculator
import com.krishifarms.mobile.feature.worker.domain.model.Worker

fun WorkerEntity.toDomain(): Worker = Worker(
    id = id,
    name = name,
    phone = phone,
    defaultHourlyRate = defaultHourlyRate,
    active = active,
    syncStatus = sync.syncStatus,
)

fun WorkOrderEntity.toDomain(): WorkOrder = WorkOrder(
    id = id,
    workerId = workerId,
    workerName = workerName,
    activityType = activityType,
    farmId = farmId,
    farmName = farmName,
    startTime = startTime,
    endTime = endTime,
    durationMinutes = durationMinutes,
    hourlyRate = hourlyRate,
    cost = cost,
    syncStatus = sync.syncStatus,
)

fun WorkOrderEntity.toWorkOrderDomain(): WorkOrder = toDomain()

fun AttendanceEntity.toDomain(workerName: String = ""): Attendance = Attendance(
    id = id,
    workerId = workerId,
    workerName = workerName,
    date = date,
    checkIn = checkIn,
    checkOut = checkOut,
    status = status,
    syncStatus = sync.syncStatus,
)

fun WorkerDtos.WorkerDto.toEntity(localId: String? = null): WorkerEntity = WorkerEntity(
    id = localId ?: id,
    name = name,
    phone = phone,
    defaultHourlyRate = defaultHourlyRate,
    active = active,
    sync = SyncMetadata(
        syncStatus = SyncStatus.SYNCED,
        lastSyncedAt = updatedAt ?: System.currentTimeMillis(),
    ),
)

fun WorkerDtos.WorkOrderDto.toEntity(localId: String? = null): WorkOrderEntity = WorkOrderEntity(
    id = localId ?: id,
    workerId = workerId,
    workerName = workerName,
    activityType = activityType,
    farmId = farmId,
    farmName = farmName,
    startTime = startTime,
    endTime = endTime,
    durationMinutes = durationMinutes,
    hourlyRate = hourlyRate,
    cost = cost,
    sync = SyncMetadata(
        syncStatus = SyncStatus.SYNCED,
        lastSyncedAt = updatedAt ?: System.currentTimeMillis(),
    ),
)

fun WorkerDtos.AttendanceDto.toEntity(localId: String? = null): AttendanceEntity = AttendanceEntity(
    id = localId ?: id,
    workerId = workerId,
    date = date,
    checkIn = checkIn,
    checkOut = checkOut,
    status = AttendanceStatus.valueOf(status),
    sync = SyncMetadata(
        syncStatus = SyncStatus.SYNCED,
        lastSyncedAt = updatedAt ?: System.currentTimeMillis(),
    ),
)

fun Worker.toEntity(syncStatus: SyncStatus, existingSync: SyncMetadata? = null): WorkerEntity = WorkerEntity(
    id = id,
    name = name,
    phone = phone,
    defaultHourlyRate = defaultHourlyRate,
    active = active,
    sync = (existingSync ?: SyncMetadata()).copy(
        syncStatus = syncStatus,
        localUpdatedAt = System.currentTimeMillis(),
    ),
)

fun buildWorkOrderEntity(
    id: String,
    workerId: String,
    workerName: String,
    activityType: String,
    farmId: String?,
    farmName: String,
    startTime: Long,
    endTime: Long,
    hourlyRate: Double,
    syncStatus: SyncStatus,
    existingSync: SyncMetadata? = null,
): WorkOrderEntity {
    val duration = WorkOrderCalculator.durationMinutes(startTime, endTime)
    val cost = WorkOrderCalculator.cost(duration, hourlyRate)
    return WorkOrderEntity(
        id = id,
        workerId = workerId,
        workerName = workerName,
        activityType = activityType,
        farmId = farmId,
        farmName = farmName,
        startTime = startTime,
        endTime = endTime,
        durationMinutes = duration,
        hourlyRate = hourlyRate,
        cost = cost,
        sync = (existingSync ?: SyncMetadata()).copy(
            syncStatus = syncStatus,
            localUpdatedAt = System.currentTimeMillis(),
        ),
    )
}
