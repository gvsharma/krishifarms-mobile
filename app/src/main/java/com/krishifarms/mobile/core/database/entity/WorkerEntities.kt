package com.krishifarms.mobile.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workers")
data class WorkerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phone: String? = null,
    @ColumnInfo(name = "default_hourly_rate")
    val defaultHourlyRate: Double,
    val active: Boolean = true,
    @Embedded
    val sync: SyncMetadata = SyncMetadata(),
)

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    HALF_DAY,
}

@Entity(tableName = "work_orders")
data class WorkOrderEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "worker_id")
    val workerId: String,
    @ColumnInfo(name = "worker_name")
    val workerName: String,
    @ColumnInfo(name = "activity_type")
    val activityType: String,
    @ColumnInfo(name = "farm_id")
    val farmId: String? = null,
    @ColumnInfo(name = "farm_name")
    val farmName: String,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,
    @ColumnInfo(name = "hourly_rate")
    val hourlyRate: Double,
    val cost: Double,
    @Embedded
    val sync: SyncMetadata = SyncMetadata(),
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "worker_id")
    val workerId: String,
    val date: Long,
    @ColumnInfo(name = "check_in")
    val checkIn: Long? = null,
    @ColumnInfo(name = "check_out")
    val checkOut: Long? = null,
    val status: AttendanceStatus,
    @Embedded
    val sync: SyncMetadata = SyncMetadata(),
)
