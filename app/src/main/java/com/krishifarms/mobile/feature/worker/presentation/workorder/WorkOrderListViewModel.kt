package com.krishifarms.mobile.feature.worker.presentation.workorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.common.Resource
import com.krishifarms.mobile.feature.worker.domain.model.FarmOption
import com.krishifarms.mobile.feature.worker.domain.model.WorkOrder
import com.krishifarms.mobile.feature.worker.domain.model.Worker
import com.krishifarms.mobile.feature.worker.domain.repository.FarmLookupRepository
import com.krishifarms.mobile.feature.worker.domain.repository.WorkOrderRepository
import com.krishifarms.mobile.feature.worker.domain.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkOrderListUiState(
    val workOrders: List<WorkOrder> = emptyList(),
    val workers: List<Worker> = emptyList(),
    val farms: List<FarmOption> = emptyList(),
    val filterWorkerId: String? = null,
    val filterActivityType: String? = null,
    val filterFarmId: String? = null,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class WorkOrderListViewModel @Inject constructor(
    private val workOrderRepository: WorkOrderRepository,
    workerRepository: WorkerRepository,
    farmLookupRepository: FarmLookupRepository,
) : ViewModel() {

    private val filterWorkerId = MutableStateFlow<String?>(null)
    private val filterActivityType = MutableStateFlow<String?>(null)
    private val filterFarmId = MutableStateFlow<String?>(null)
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val filters = combine(filterWorkerId, filterActivityType, filterFarmId) { w, a, f ->
        Triple(w, a, f)
    }

    val uiState: StateFlow<WorkOrderListUiState> = combine(
        filters.flatMapLatest { (workerId, activityType, farmId) ->
            workOrderRepository.observeWorkOrders(workerId, activityType, farmId)
        },
        workerRepository.observeWorkers(),
        farmLookupRepository.observeFarms(),
        filterWorkerId,
        filterActivityType,
        filterFarmId,
        isRefreshing,
        errorMessage,
    ) { workOrders, workers, farms, workerId, activityType, farmId, refreshing, error ->
        WorkOrderListUiState(
            workOrders = workOrders,
            workers = workers,
            farms = farms,
            filterWorkerId = workerId,
            filterActivityType = activityType,
            filterFarmId = farmId,
            isRefreshing = refreshing,
            errorMessage = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorkOrderListUiState())

    fun setWorkerFilter(workerId: String?) {
        filterWorkerId.value = workerId
    }

    fun setActivityFilter(activityType: String?) {
        filterActivityType.value = activityType
    }

    fun setFarmFilter(farmId: String?) {
        filterFarmId.value = farmId
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            when (val result = workOrderRepository.syncWorkOrders()) {
                is Resource.Error -> errorMessage.value = result.message
                else -> Unit
            }
            isRefreshing.value = false
        }
    }
}
