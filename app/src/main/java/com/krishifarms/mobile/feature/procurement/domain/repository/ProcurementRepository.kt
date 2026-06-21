package com.krishifarms.mobile.feature.procurement.domain.repository

import com.krishifarms.mobile.feature.procurement.domain.model.FarmerOption
import com.krishifarms.mobile.feature.procurement.domain.model.Procurement
import kotlinx.coroutines.flow.Flow

data class CreateProcurementInput(
    val farmerId: String,
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
)

interface ProcurementRepository {
    fun observeProcurements(): Flow<List<Procurement>>
    fun observeProcurement(id: String): Flow<Procurement?>
    fun observeFarmerOptions(): Flow<List<FarmerOption>>
    suspend fun createProcurement(input: CreateProcurementInput): Result<String>
    suspend fun refreshFromServer()
    suspend fun syncPending()
}
