package com.krishifarms.mobile.feature.dashboard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dashboard_summary_cache")
data class DashboardSummaryEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val todaysProcurementAmount: Double?,
    val todaysProcurementCount: Int,
    val todaysExpensesAmount: Double?,
    val todaysExpensesCount: Int,
    val todaysCollectionsAmount: Double?,
    val todaysCollectionsCount: Int,
    val pendingFarmerPaymentsAmount: Double?,
    val pendingFarmerPaymentsCount: Int,
    val pendingCollectionsAmount: Double?,
    val pendingCollectionsCount: Int,
    val workerAttendanceAmount: Double?,
    val workerAttendanceCount: Int,
    val activeRentalsAmount: Double?,
    val activeRentalsCount: Int,
    val currency: String,
    val cachedAt: Long,
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
