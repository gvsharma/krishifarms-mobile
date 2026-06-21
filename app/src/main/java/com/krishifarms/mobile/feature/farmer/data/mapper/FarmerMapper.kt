package com.krishifarms.mobile.feature.farmer.data.mapper

import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.entity.FarmerEntity
import com.krishifarms.mobile.core.database.entity.SyncMetadata
import com.krishifarms.mobile.core.data.mapper.toCropTypeList
import com.krishifarms.mobile.core.data.mapper.toCropTypesStorage
import com.krishifarms.mobile.core.network.dto.FarmerDtos
import com.krishifarms.mobile.feature.farmer.domain.model.Farmer

fun FarmerEntity.toDomain(): Farmer = Farmer(
    id = id,
    serverId = serverId,
    name = name,
    village = village,
    phone = phone,
    bankDetails = bankDetails,
    landAcres = landAcres,
    cropTypes = cropTypes.toCropTypeList(),
    syncStatus = sync.syncStatus,
)

fun FarmerDtos.FarmerDto.toEntity(localId: String? = null): FarmerEntity = FarmerEntity(
    id = localId ?: id,
    serverId = id,
    name = name,
    village = village,
    phone = phone,
    bankDetails = bankDetails,
    landAcres = landAcres,
    cropTypes = cropTypes.toCropTypesStorage(),
    sync = SyncMetadata(
        syncStatus = SyncStatus.SYNCED,
        lastSyncedAt = updatedAt,
        localUpdatedAt = updatedAt,
    ),
)

fun Farmer.toEntity(syncStatus: SyncStatus, existingSync: SyncMetadata? = null): FarmerEntity = FarmerEntity(
    id = id,
    serverId = serverId,
    name = name,
    village = village,
    phone = phone,
    bankDetails = bankDetails,
    landAcres = landAcres,
    cropTypes = cropTypes.toCropTypesStorage(),
    sync = (existingSync ?: SyncMetadata()).copy(
        syncStatus = syncStatus,
        localUpdatedAt = System.currentTimeMillis(),
    ),
)
