package com.krishifarms.mobile.core.data.mapper

import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.entity.FarmerEntity
import com.krishifarms.mobile.core.database.entity.FarmEntity
import com.krishifarms.mobile.core.database.entity.PaymentEntity
import com.krishifarms.mobile.core.database.entity.SyncMetadata
import com.krishifarms.mobile.core.domain.model.Farmer
import com.krishifarms.mobile.core.domain.model.Farm
import com.krishifarms.mobile.core.domain.model.Payment
import com.krishifarms.mobile.core.network.dto.FarmerDtos
import com.krishifarms.mobile.core.network.dto.FarmDtos
import com.krishifarms.mobile.core.network.dto.PaymentDtos

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
    cropTypes = cropTypes.joinToString(","),
    sync = SyncMetadata(
        syncStatus = SyncStatus.SYNCED,
        lastSyncedAt = updatedAt,
        localUpdatedAt = updatedAt,
    ),
)

fun String.toCropTypeList(): List<String> =
    split(",").map { it.trim() }.filter { it.isNotEmpty() }

fun List<String>.toCropTypesStorage(): String = joinToString(",")

fun FarmDtos.FarmDto.toEntity(): FarmEntity = FarmEntity(
    id = id,
    farmerId = farmerId,
    name = name,
    acreage = acreage,
    cropType = cropType,
    latitude = latitude,
    longitude = longitude,
    sync = SyncMetadata(
        syncStatus = SyncStatus.SYNCED,
        lastSyncedAt = updatedAt,
        localUpdatedAt = updatedAt,
    ),
)

fun FarmEntity.toDomain(): Farm = Farm(
    id = id,
    farmerId = farmerId,
    name = name,
    acreage = acreage,
    cropType = cropType,
    latitude = latitude,
    longitude = longitude,
    syncStatus = sync.syncStatus,
)

fun PaymentDtos.PaymentDto.toEntity(): PaymentEntity = PaymentEntity(
    id = id,
    farmerId = farmerId,
    amount = amount,
    paymentMode = paymentMode,
    referenceNumber = referenceNumber,
    paidAt = paidAt,
    sync = SyncMetadata(
        syncStatus = SyncStatus.SYNCED,
        lastSyncedAt = updatedAt,
        localUpdatedAt = updatedAt,
    ),
)

fun PaymentEntity.toDomain(): Payment = Payment(
    id = id,
    farmerId = farmerId,
    amount = amount,
    paymentMode = paymentMode,
    referenceNumber = referenceNumber,
    paidAt = paidAt,
    syncStatus = sync.syncStatus,
)

fun Farmer.toEntity(pending: SyncStatus = SyncStatus.PENDING_CREATE, existingSync: SyncMetadata? = null): FarmerEntity =
    FarmerEntity(
        id = id,
        serverId = serverId,
        name = name,
        village = village,
        phone = phone,
        bankDetails = bankDetails,
        landAcres = landAcres,
        cropTypes = cropTypes.joinToString(","),
        sync = (existingSync ?: SyncMetadata()).copy(
            syncStatus = pending,
            localUpdatedAt = System.currentTimeMillis(),
        ),
    )
