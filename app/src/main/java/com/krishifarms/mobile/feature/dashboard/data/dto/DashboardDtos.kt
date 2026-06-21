package com.krishifarms.mobile.feature.dashboard.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardSummaryResponse(
    @SerialName("todays_procurement") val todaysProcurement: DashboardMetricDto,
    @SerialName("todays_expenses") val todaysExpenses: DashboardMetricDto,
    @SerialName("todays_collections") val todaysCollections: DashboardMetricDto,
    @SerialName("pending_farmer_payments") val pendingFarmerPayments: DashboardMetricDto,
    @SerialName("pending_collections") val pendingCollections: DashboardMetricDto,
    @SerialName("worker_attendance") val workerAttendance: DashboardMetricDto,
    @SerialName("active_rentals") val activeRentals: DashboardMetricDto,
    @SerialName("last_updated_at") val lastUpdatedAt: String? = null,
)

@Serializable
data class DashboardMetricDto(
    val amount: Double? = null,
    val count: Int = 0,
    val currency: String = "INR",
)
