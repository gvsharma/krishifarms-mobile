package com.krishifarms.mobile.feature.dashboard.data.mapper

import com.krishifarms.mobile.feature.dashboard.data.dto.DashboardMetricDto
import com.krishifarms.mobile.feature.dashboard.data.dto.DashboardSummaryResponse
import com.krishifarms.mobile.feature.dashboard.data.local.entity.DashboardSummaryEntity
import com.krishifarms.mobile.feature.dashboard.domain.model.DashboardMetric
import com.krishifarms.mobile.feature.dashboard.domain.model.DashboardSummary
import java.time.Instant

fun DashboardSummaryResponse.toDomain(fetchedAt: Long = System.currentTimeMillis()): DashboardSummary {
    return DashboardSummary(
        todaysProcurement = todaysProcurement.toDomain(),
        todaysExpenses = todaysExpenses.toDomain(),
        todaysCollections = todaysCollections.toDomain(),
        pendingFarmerPayments = pendingFarmerPayments.toDomain(),
        pendingCollections = pendingCollections.toDomain(),
        workerAttendance = workerAttendance.toDomain(),
        activeRentals = activeRentals.toDomain(),
        lastUpdatedAt = lastUpdatedAt?.let(::parseTimestamp) ?: fetchedAt,
        isFromCache = false,
    )
}

fun DashboardSummaryEntity.toDomain(): DashboardSummary {
    return DashboardSummary(
        todaysProcurement = metric(todaysProcurementAmount, todaysProcurementCount),
        todaysExpenses = metric(todaysExpensesAmount, todaysExpensesCount),
        todaysCollections = metric(todaysCollectionsAmount, todaysCollectionsCount),
        pendingFarmerPayments = metric(pendingFarmerPaymentsAmount, pendingFarmerPaymentsCount),
        pendingCollections = metric(pendingCollectionsAmount, pendingCollectionsCount),
        workerAttendance = metric(workerAttendanceAmount, workerAttendanceCount),
        activeRentals = metric(activeRentalsAmount, activeRentalsCount),
        lastUpdatedAt = cachedAt,
        isFromCache = true,
    )
}

fun DashboardSummary.toEntity(): DashboardSummaryEntity {
    return DashboardSummaryEntity(
        todaysProcurementAmount = todaysProcurement.amount,
        todaysProcurementCount = todaysProcurement.count,
        todaysExpensesAmount = todaysExpenses.amount,
        todaysExpensesCount = todaysExpenses.count,
        todaysCollectionsAmount = todaysCollections.amount,
        todaysCollectionsCount = todaysCollections.count,
        pendingFarmerPaymentsAmount = pendingFarmerPayments.amount,
        pendingFarmerPaymentsCount = pendingFarmerPayments.count,
        pendingCollectionsAmount = pendingCollections.amount,
        pendingCollectionsCount = pendingCollections.count,
        workerAttendanceAmount = workerAttendance.amount,
        workerAttendanceCount = workerAttendance.count,
        activeRentalsAmount = activeRentals.amount,
        activeRentalsCount = activeRentals.count,
        currency = todaysProcurement.currency,
        cachedAt = lastUpdatedAt,
    )
}

private fun DashboardMetricDto.toDomain(): DashboardMetric {
    return DashboardMetric(
        amount = amount,
        count = count,
        currency = currency,
    )
}

private fun metric(amount: Double?, count: Int, currency: String = "INR"): DashboardMetric {
    return DashboardMetric(
        amount = amount,
        count = count,
        currency = currency,
    )
}

private fun parseTimestamp(value: String): Long {
    return runCatching { Instant.parse(value).toEpochMilli() }
        .getOrDefault(System.currentTimeMillis())
}
