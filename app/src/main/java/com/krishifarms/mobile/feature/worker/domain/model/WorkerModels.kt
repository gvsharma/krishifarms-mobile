package com.krishifarms.mobile.feature.worker.domain.model

import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.entity.AttendanceStatus

data class Worker(
    val id: String,
    val name: String,
    val phone: String?,
    val defaultHourlyRate: Double,
    val active: Boolean,
    val syncStatus: SyncStatus,
)

data class WorkOrder(
    val id: String,
    val workerId: String,
    val workerName: String,
    val activityType: String,
    val farmId: String?,
    val farmName: String,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val hourlyRate: Double,
    val cost: Double,
    val syncStatus: SyncStatus,
)

data class Attendance(
    val id: String,
    val workerId: String,
    val workerName: String,
    val date: Long,
    val checkIn: Long?,
    val checkOut: Long?,
    val status: AttendanceStatus,
    val syncStatus: SyncStatus,
)

data class FarmOption(
    val id: String,
    val name: String,
)

enum class ActivityType(val label: String) {
    PLOUGHING("Ploughing"),
    SOWING("Sowing"),
    IRRIGATION("Irrigation"),
    WEEDING("Weeding"),
    HARVESTING("Harvesting"),
    FERTILIZER("Fertilizer Application"),
    SPRAYING("Spraying"),
    TRANSPORT("Transport"),
    OTHER("Other"),
}

object WorkOrderCalculator {
    fun durationMinutes(startTime: Long, endTime: Long): Int {
        if (endTime <= startTime) return 0
        return ((endTime - startTime) / 60_000L).toInt()
    }

    fun cost(durationMinutes: Int, hourlyRate: Double): Double {
        if (durationMinutes <= 0) return 0.0
        return (durationMinutes / 60.0) * hourlyRate
    }
}

object DateUtils {
    fun startOfDay(epochMillis: Long): Long {
        val dayMs = 86_400_000L
        return (epochMillis / dayMs) * dayMs
    }
}
