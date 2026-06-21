package com.krishifarms.mobile.feature.worker.domain.repository

import com.krishifarms.mobile.core.common.Resource
import com.krishifarms.mobile.core.database.entity.AttendanceStatus
import com.krishifarms.mobile.feature.worker.domain.model.Attendance
import com.krishifarms.mobile.feature.worker.domain.model.FarmOption
import com.krishifarms.mobile.feature.worker.domain.model.WorkOrder
import com.krishifarms.mobile.feature.worker.domain.model.Worker
import kotlinx.coroutines.flow.Flow

interface WorkerRepository {
    fun observeWorkers(searchQuery: String = ""): Flow<List<Worker>>
    fun observeWorker(id: String): Flow<Worker?>
    suspend fun getWorker(id: String): Worker?
    suspend fun saveWorker(id: String?, name: String, phone: String?, hourlyRate: Double): Resource<Worker>
    suspend fun syncWorkers(): Resource<Unit>
}

interface WorkOrderRepository {
    fun observeWorkOrders(
        workerId: String? = null,
        activityType: String? = null,
        farmId: String? = null,
    ): Flow<List<WorkOrder>>

    fun observeWorkOrder(id: String): Flow<WorkOrder?>
    suspend fun saveWorkOrder(
        id: String?,
        workerId: String,
        workerName: String,
        activityType: String,
        farmId: String?,
        farmName: String,
        startTime: Long,
        endTime: Long,
        hourlyRate: Double,
    ): Resource<WorkOrder>

    suspend fun syncWorkOrders(): Resource<Unit>
}

interface AttendanceRepository {
    fun observeAttendanceForDate(date: Long): Flow<List<Attendance>>
    fun observeAttendanceForWorker(workerId: String): Flow<List<Attendance>>
    suspend fun markAttendance(
        workerId: String,
        date: Long,
        status: AttendanceStatus,
        checkIn: Long? = null,
        checkOut: Long? = null,
    ): Resource<Attendance>

    suspend fun syncAttendance(): Resource<Unit>
}

interface FarmLookupRepository {
    fun observeFarms(): Flow<List<FarmOption>>
}
