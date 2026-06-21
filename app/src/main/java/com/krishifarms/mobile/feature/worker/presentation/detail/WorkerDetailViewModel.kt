package com.krishifarms.mobile.feature.worker.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.navigation.Routes
import com.krishifarms.mobile.feature.worker.domain.model.Attendance
import com.krishifarms.mobile.feature.worker.domain.model.WorkOrder
import com.krishifarms.mobile.feature.worker.domain.model.Worker
import com.krishifarms.mobile.feature.worker.domain.repository.AttendanceRepository
import com.krishifarms.mobile.feature.worker.domain.repository.WorkOrderRepository
import com.krishifarms.mobile.feature.worker.domain.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class WorkerDetailTab {
    INFO,
    ATTENDANCE,
    WORK_ORDERS,
}

data class WorkerDetailUiState(
    val worker: Worker? = null,
    val selectedTab: WorkerDetailTab = WorkerDetailTab.INFO,
    val attendance: List<Attendance> = emptyList(),
    val workOrders: List<WorkOrder> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class WorkerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    workerRepository: WorkerRepository,
    attendanceRepository: AttendanceRepository,
    workOrderRepository: WorkOrderRepository,
) : ViewModel() {

    private val workerId: String = checkNotNull(savedStateHandle.get<String>(Routes.WORKER_ID_ARG))

    private val selectedTab = kotlinx.coroutines.flow.MutableStateFlow(WorkerDetailTab.INFO)

    val uiState: StateFlow<WorkerDetailUiState> = combine(
        workerRepository.observeWorker(workerId),
        selectedTab,
        attendanceRepository.observeAttendanceForWorker(workerId),
        workOrderRepository.observeWorkOrders(workerId = workerId),
    ) { worker, tab, attendance, workOrders ->
        WorkerDetailUiState(
            worker = worker,
            selectedTab = tab,
            attendance = attendance,
            workOrders = workOrders,
            isLoading = worker == null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorkerDetailUiState())

    fun selectTab(tab: WorkerDetailTab) {
        selectedTab.value = tab
    }
}
