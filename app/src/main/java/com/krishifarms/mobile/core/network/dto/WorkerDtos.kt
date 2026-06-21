package com.krishifarms.mobile.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object WorkerDtos {

    @Serializable
    data class WorkerListResponse(
        val items: List<WorkerDto>,
        val total: Int = 0,
        @SerialName("has_more") val hasMore: Boolean = false,
    )

    @Serializable
    data class WorkerDto(
        val id: String,
        val name: String,
        val phone: String? = null,
        @SerialName("default_hourly_rate") val defaultHourlyRate: Double,
        val active: Boolean = true,
        @SerialName("updated_at") val updatedAt: Long? = null,
    )

    @Serializable
    data class CreateWorkerRequest(
        val name: String,
        val phone: String? = null,
        @SerialName("default_hourly_rate") val defaultHourlyRate: Double,
    )

    @Serializable
    data class UpdateWorkerRequest(
        val name: String,
        val phone: String? = null,
        @SerialName("default_hourly_rate") val defaultHourlyRate: Double,
        val active: Boolean = true,
    )

    @Serializable
    data class WorkOrderListResponse(
        val items: List<WorkOrderDto>,
        val total: Int = 0,
        @SerialName("has_more") val hasMore: Boolean = false,
    )

    @Serializable
    data class WorkOrderDto(
        val id: String,
        @SerialName("worker_id") val workerId: String,
        @SerialName("worker_name") val workerName: String,
        @SerialName("activity_type") val activityType: String,
        @SerialName("farm_id") val farmId: String? = null,
        @SerialName("farm_name") val farmName: String,
        @SerialName("start_time") val startTime: Long,
        @SerialName("end_time") val endTime: Long,
        @SerialName("duration_minutes") val durationMinutes: Int,
        @SerialName("hourly_rate") val hourlyRate: Double,
        val cost: Double,
        @SerialName("updated_at") val updatedAt: Long? = null,
    )

    @Serializable
    data class CreateWorkOrderRequest(
        @SerialName("worker_id") val workerId: String,
        @SerialName("activity_type") val activityType: String,
        @SerialName("farm_id") val farmId: String? = null,
        @SerialName("farm_name") val farmName: String,
        @SerialName("start_time") val startTime: Long,
        @SerialName("end_time") val endTime: Long,
        @SerialName("hourly_rate") val hourlyRate: Double,
    )

    @Serializable
    data class AttendanceListResponse(
        val items: List<AttendanceDto>,
        val total: Int = 0,
    )

    @Serializable
    data class AttendanceDto(
        val id: String,
        @SerialName("worker_id") val workerId: String,
        val date: Long,
        @SerialName("check_in") val checkIn: Long? = null,
        @SerialName("check_out") val checkOut: Long? = null,
        val status: String,
        @SerialName("updated_at") val updatedAt: Long? = null,
    )

    @Serializable
    data class UpsertAttendanceRequest(
        @SerialName("worker_id") val workerId: String,
        val date: Long,
        @SerialName("check_in") val checkIn: Long? = null,
        @SerialName("check_out") val checkOut: Long? = null,
        val status: String,
    )
}
