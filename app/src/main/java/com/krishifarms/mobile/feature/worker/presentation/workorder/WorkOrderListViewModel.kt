package com.krishifarms.mobile.feature.worker.presentation.workorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.common.Resource
import com.krishifarms.mobile.core.security.rbac.ActionPermissions
import com.krishifarms.mobile.core.security.rbac.PermissionManager
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
    val canCreate: Boolean = false,
)

@HiltViewModel
class WorkOrderListViewModel @Inject constructor(
    private val workOrderRepository: WorkOrderRepository,
    workerRepository: WorkerRepository,
    farmLookupRepository: FarmLookupRepository,
    permissionManager: PermissionManager,
) : ViewModel() {

    private val canCreate = ActionPermissions.from(permissionManager).workOrder.canCreate

    private val filterWorkerId = MutableStateFlow<String?>(null)
    private val filterActivityType = MutableStateFlow<String?>(null)
    private val filterFarmId = MutableStateFlow<String?>(null)
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val filters = combine(filterWorkerId, filterActivityType, filterFarmId) { w, a, f ->
        Triple(w, a, f)
    }

    private val listData = combine(
        filters.flatMapLatest { (workerId, activityType, farmId) ->
            workOrderRepository.observeWorkOrders(workerId, activityType, farmId)
        },
        workerRepository.observeWorkers(),
        farmLookupRepository.observeFarms(),
    ) { workOrders, workers, farms ->
        Triple(workOrders, workers, farms)
    }

    private val filterState = combine(
        filterWorkerId,
        filterActivityType,
        filterFarmId,
        isRefreshing,
        errorMessage,
    ) { workerId, activityType, farmId, refreshing, error ->
        FilterUiState(workerId, activityType, farmId, refreshing, error)
    }

    val uiState: StateFlow<WorkOrderListUiState> = combine(listData, filterState) { data, filters ->
        WorkOrderListUiState(
            workOrders = data.first,
            workers = data.second,
            farms = data.third,
            filterWorkerId = filters.workerId,
            filterActivityType = filters.activityType,
            filterFarmId = filters.farmId,
            isRefreshing = filters.refreshing,
            errorMessage = filters.error,
            canCreate = canCreate,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorkOrderListUiState(canCreate = canCreate))

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

private data class FilterUiState(
    val workerId: String?,
    val activityType: String?,
    val farmId: String?,
    val refreshing: Boolean,
    val error: String?,
)
