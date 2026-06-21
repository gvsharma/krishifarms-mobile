package com.krishifarms.mobile.feature.worker.presentation.workorder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.common.Resource
import com.krishifarms.mobile.core.navigation.Routes
import com.krishifarms.mobile.feature.worker.domain.model.ActivityType
import com.krishifarms.mobile.feature.worker.domain.model.FarmOption
import com.krishifarms.mobile.feature.worker.domain.model.WorkOrderCalculator
import com.krishifarms.mobile.feature.worker.domain.model.Worker
import com.krishifarms.mobile.feature.worker.domain.repository.FarmLookupRepository
import com.krishifarms.mobile.feature.worker.domain.repository.WorkOrderRepository
import com.krishifarms.mobile.feature.worker.domain.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkOrderFormUiState(
    val workOrderId: String? = null,
    val workers: List<Worker> = emptyList(),
    val farms: List<FarmOption> = emptyList(),
    val selectedWorkerId: String? = null,
    val selectedFarmId: String? = null,
    val activityType: String = ActivityType.OTHER.label,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis() + 3_600_000L,
    val hourlyRate: String = "",
    val durationMinutes: Int = 60,
    val cost: Double = 0.0,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class WorkOrderFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workOrderRepository: WorkOrderRepository,
    workerRepository: WorkerRepository,
    farmLookupRepository: FarmLookupRepository,
) : ViewModel() {

    private val workOrderId: String? = savedStateHandle.get<String>(Routes.WORK_ORDER_ID_ARG)

    private val _uiState = MutableStateFlow(WorkOrderFormUiState(workOrderId = workOrderId))
    val uiState: StateFlow<WorkOrderFormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            workerRepository.observeWorkers().collect { workers ->
                _uiState.update { state ->
                    val selectedWorker = workers.find { it.id == state.selectedWorkerId } ?: workers.firstOrNull()
                    val rate = selectedWorker?.defaultHourlyRate?.toString().orEmpty()
                    recalculate(
                        state.copy(
                            workers = workers,
                            selectedWorkerId = selectedWorker?.id,
                            hourlyRate = state.hourlyRate.ifBlank { rate },
                        ),
                    )
                }
            }
        }
        viewModelScope.launch {
            farmLookupRepository.observeFarms().collect { farms ->
                _uiState.update {
                    it.copy(
                        farms = farms,
                        selectedFarmId = it.selectedFarmId ?: farms.firstOrNull()?.id,
                    )
                }
            }
        }
        workOrderId?.let { loadWorkOrder(it) }
    }

    fun onWorkerSelected(workerId: String) {
        val worker = _uiState.value.workers.find { it.id == workerId }
        _uiState.update {
            recalculate(
                it.copy(
                    selectedWorkerId = workerId,
                    hourlyRate = worker?.defaultHourlyRate?.toString().orEmpty(),
                ),
            )
        }
    }

    fun onFarmSelected(farmId: String) {
        _uiState.update { it.copy(selectedFarmId = farmId) }
    }

    fun onActivityTypeSelected(activityType: String) {
        _uiState.update { it.copy(activityType = activityType) }
    }

    fun onStartTimeChanged(timeMillis: Long) {
        _uiState.update { recalculate(it.copy(startTime = timeMillis)) }
    }

    fun onEndTimeChanged(timeMillis: Long) {
        _uiState.update { recalculate(it.copy(endTime = timeMillis)) }
    }

    fun onHourlyRateChanged(value: String) {
        _uiState.update { recalculate(it.copy(hourlyRate = value.filter { it.isDigit() || it == '.' })) }
    }

    fun save() {
        val state = _uiState.value
        val worker = state.workers.find { it.id == state.selectedWorkerId }
        val farm = state.farms.find { it.id == state.selectedFarmId }
        val rate = state.hourlyRate.toDoubleOrNull()

        if (worker == null || farm == null || rate == null || rate <= 0) {
            _uiState.update { it.copy(errorMessage = "Please fill all required fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (
                val result = workOrderRepository.saveWorkOrder(
                    id = state.workOrderId,
                    workerId = worker.id,
                    workerName = worker.name,
                    activityType = state.activityType,
                    farmId = farm.id,
                    farmName = farm.name,
                    startTime = state.startTime,
                    endTime = state.endTime,
                    hourlyRate = rate,
                )
            ) {
                is Resource.Success -> _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun loadWorkOrder(id: String) {
        viewModelScope.launch {
            workOrderRepository.observeWorkOrder(id).collect { order ->
                order ?: return@collect
                _uiState.update {
                    recalculate(
                        it.copy(
                            selectedWorkerId = order.workerId,
                            selectedFarmId = order.farmId,
                            activityType = order.activityType,
                            startTime = order.startTime,
                            endTime = order.endTime,
                            hourlyRate = order.hourlyRate.toString(),
                        ),
                    )
                }
            }
        }
    }

    private fun recalculate(state: WorkOrderFormUiState): WorkOrderFormUiState {
        val duration = WorkOrderCalculator.durationMinutes(state.startTime, state.endTime)
        val rate = state.hourlyRate.toDoubleOrNull() ?: 0.0
        val cost = WorkOrderCalculator.cost(duration, rate)
        return state.copy(durationMinutes = duration, cost = cost)
    }
}
