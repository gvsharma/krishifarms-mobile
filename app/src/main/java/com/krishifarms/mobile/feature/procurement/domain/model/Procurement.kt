package com.krishifarms.mobile.feature.procurement.domain.model

import com.krishifarms.mobile.core.common.SyncStatus

data class Procurement(
    val id: String,
    val serverId: String?,
    val farmerId: String,
    val farmerName: String?,
    val farmerVillage: String?,
    val crop: String,
    val village: String,
    val bags: Int,
    val weight: Double,
    val moisture: Double,
    val rate: Double,
    val deductions: Double,
    val netAmount: Double,
    val localImagePath: String?,
    val localBillPath: String?,
    val remoteImageUrl: String?,
    val remoteBillUrl: String?,
    val syncStatus: SyncStatus,
    val createdAt: Long,
)

data class FarmerOption(
    val id: String,
    val name: String,
    val village: String,
)
