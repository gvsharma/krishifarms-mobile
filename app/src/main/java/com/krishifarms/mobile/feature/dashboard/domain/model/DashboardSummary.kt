package com.krishifarms.mobile.feature.dashboard.domain.model

data class DashboardMetric(
    val amount: Double?,
    val count: Int,
    val currency: String = "INR",
)

data class DashboardSummary(
    val todaysProcurement: DashboardMetric,
    val todaysExpenses: DashboardMetric,
    val todaysCollections: DashboardMetric,
    val pendingFarmerPayments: DashboardMetric,
    val pendingCollections: DashboardMetric,
    val workerAttendance: DashboardMetric,
    val activeRentals: DashboardMetric,
    val lastUpdatedAt: Long,
    val isFromCache: Boolean = false,
)
