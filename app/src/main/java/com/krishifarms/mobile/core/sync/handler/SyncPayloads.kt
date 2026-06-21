package com.krishifarms.mobile.core.sync.handler

import kotlinx.serialization.Serializable

@Serializable
data class FarmerSyncPayload(
    val name: String,
    val phone: String,
    val village: String,
    val bankDetails: String,
    val landAcres: Double,
    val cropTypes: List<String>,
    val localUpdatedAt: Long,
)

@Serializable
data class ExpenseSyncPayload(
    val category: String,
    val description: String,
    val amount: Double,
    val expenseDate: Long,
    val vendor: String? = null,
    val paymentMethod: String? = null,
    val localUpdatedAt: Long,
)
