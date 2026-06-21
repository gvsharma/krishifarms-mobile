package com.krishifarms.mobile.feature.worker.presentation.workorder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.navigation.Routes
import com.krishifarms.mobile.feature.worker.domain.model.WorkOrder
import com.krishifarms.mobile.feature.worker.domain.repository.WorkOrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class WorkOrderDetailUiState(
    val workOrder: WorkOrder? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class WorkOrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    workOrderRepository: WorkOrderRepository,
) : ViewModel() {

    private val workOrderId: String = checkNotNull(savedStateHandle.get<String>(Routes.WORK_ORDER_ID_ARG))

    val uiState: StateFlow<WorkOrderDetailUiState> =
        workOrderRepository.observeWorkOrder(workOrderId)
            .map { order ->
                WorkOrderDetailUiState(
                    workOrder = order,
                    isLoading = order == null,
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorkOrderDetailUiState())
}
