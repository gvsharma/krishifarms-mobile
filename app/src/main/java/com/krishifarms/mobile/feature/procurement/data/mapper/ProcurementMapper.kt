package com.krishifarms.mobile.feature.procurement.data.mapper

import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.entity.ProcurementEntity
import com.krishifarms.mobile.core.database.entity.ProcurementWithFarmer
import com.krishifarms.mobile.core.database.entity.SyncMetadata
import com.krishifarms.mobile.feature.procurement.data.remote.dto.ProcurementDto
import com.krishifarms.mobile.feature.procurement.domain.model.Procurement

fun ProcurementWithFarmer.toDomain(): Procurement = procurement.toDomain(
    farmerName = farmerName,
    farmerVillage = farmerVillage,
)

fun ProcurementEntity.toDomain(
    farmerName: String? = null,
    farmerVillage: String? = null,
): Procurement = Procurement(
    id = id,
    serverId = serverId,
    farmerId = farmerId,
    farmerName = farmerName,
    farmerVillage = farmerVillage,
    crop = crop,
    village = village,
    bags = bags,
    weight = weight,
    moisture = moisture,
    rate = rate,
    deductions = deductions,
    netAmount = netAmount,
    localImagePath = localImagePath,
    localBillPath = localBillPath,
    remoteImageUrl = remoteImageUrl,
    remoteBillUrl = remoteBillUrl,
    syncStatus = sync.syncStatus,
    createdAt = createdAt,
)

fun ProcurementDto.toEntity(existing: ProcurementEntity? = null): ProcurementEntity = ProcurementEntity(
    id = existing?.id ?: id,
    serverId = id,
    farmerId = farmerId,
    crop = crop,
    village = village,
    bags = bags,
    weight = weight,
    moisture = moisture,
    rate = rate,
    deductions = deductions,
    netAmount = netAmount,
    localImagePath = existing?.localImagePath,
    localBillPath = existing?.localBillPath,
    remoteImageUrl = imageUrl ?: existing?.remoteImageUrl,
    remoteBillUrl = billUrl ?: existing?.remoteBillUrl,
    createdAt = createdAt,
    sync = SyncMetadata(
        syncStatus = SyncStatus.SYNCED,
        lastSyncedAt = System.currentTimeMillis(),
        localUpdatedAt = existing?.sync?.localUpdatedAt ?: createdAt,
    ),
)
