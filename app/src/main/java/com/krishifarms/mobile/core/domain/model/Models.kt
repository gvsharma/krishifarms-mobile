package com.krishifarms.mobile.core.domain.model

import com.krishifarms.mobile.core.common.SyncStatus

data class Farmer(
    val id: String,
    val serverId: String? = null,
    val name: String,
    val village: String,
    val phone: String,
    val bankDetails: String,
    val landAcres: Double,
    val cropTypes: List<String>,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

data class Farm(
    val id: String,
    val farmerId: String,
    val name: String,
    val acreage: Double,
    val cropType: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

data class Procurement(
    val id: String,
    val farmerId: String,
    val farmId: String,
    val cropName: String,
    val quantityKg: Double,
    val ratePerKg: Double,
    val totalAmount: Double,
    val procuredAt: Long,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

data class Payment(
    val id: String,
    val farmerId: String,
    val amount: Double,
    val paymentMode: String,
    val referenceNumber: String? = null,
    val paidAt: Long,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

data class Worker(
    val id: String,
    val name: String,
    val phone: String,
    val role: String,
    val assignedRegion: String,
    val active: Boolean = true,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

data class DashboardSummary(
    val farmerCount: Int,
    val farmCount: Int,
    val pendingProcurements: Int,
    val pendingPayments: Int,
    val pendingSyncCount: Int,
)
