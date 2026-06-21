package com.krishifarms.mobile.feature.procurement.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProcurementDto(
    val id: String,
    @SerialName("farmer_id") val farmerId: String,
    @SerialName("farmer_name") val farmerName: String? = null,
    val crop: String,
    val village: String,
    val bags: Int,
    val weight: Double,
    val moisture: Double,
    val rate: Double,
    val deductions: Double,
    @SerialName("net_amount") val netAmount: Double,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("bill_url") val billUrl: String? = null,
    @SerialName("created_at") val createdAt: Long,
)

@Serializable
data class CreateProcurementRequest(
    @SerialName("local_id") val localId: String,
    @SerialName("farmer_id") val farmerId: String,
    val crop: String,
    val village: String,
    val bags: Int,
    val weight: Double,
    val moisture: Double,
    val rate: Double,
    val deductions: Double,
    @SerialName("net_amount") val netAmount: Double,
)

@Serializable
data class CreateProcurementResponse(
    val procurement: ProcurementDto,
)
