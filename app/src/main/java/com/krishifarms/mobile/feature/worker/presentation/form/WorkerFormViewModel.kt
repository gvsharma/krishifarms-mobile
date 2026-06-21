package com.krishifarms.mobile.feature.worker.presentation.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.common.Resource
import com.krishifarms.mobile.core.navigation.Routes
import com.krishifarms.mobile.feature.worker.domain.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkerFormUiState(
    val workerId: String? = null,
    val name: String = "",
    val phone: String = "",
    val hourlyRate: String = "",
    val nameError: String? = null,
    val hourlyRateError: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class WorkerFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workerRepository: WorkerRepository,
) : ViewModel() {

    private val workerId: String? = savedStateHandle.get<String>(Routes.WORKER_ID_ARG)

    private val _uiState = MutableStateFlow(WorkerFormUiState(workerId = workerId))
    val uiState: StateFlow<WorkerFormUiState> = _uiState.asStateFlow()

    init {
        workerId?.let { loadWorker(it) }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, errorMessage = null) }
    }

    fun onPhoneChanged(value: String) {
        _uiState.update { it.copy(phone = value.filter { it.isDigit() || it == '+' }.take(13)) }
    }

    fun onHourlyRateChanged(value: String) {
        _uiState.update { it.copy(hourlyRate = value.filter { it.isDigit() || it == '.' }, hourlyRateError = null) }
    }

    fun save() {
        val state = _uiState.value
        val nameError = if (state.name.isBlank()) "Name is required" else null
        val rate = state.hourlyRate.toDoubleOrNull()
        val hourlyRateError = when {
            state.hourlyRate.isBlank() -> "Hourly rate is required"
            rate == null || rate <= 0 -> "Enter a valid hourly rate"
            else -> null
        }

        if (nameError != null || hourlyRateError != null) {
            _uiState.update { it.copy(nameError = nameError, hourlyRateError = hourlyRateError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (
                val result = workerRepository.saveWorker(
                    id = state.workerId,
                    name = state.name,
                    phone = state.phone.ifBlank { null },
                    hourlyRate = rate!!,
                )
            ) {
                is Resource.Success -> _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun loadWorker(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val worker = workerRepository.getWorker(id)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = worker?.name.orEmpty(),
                    phone = worker?.phone.orEmpty(),
                    hourlyRate = worker?.defaultHourlyRate?.toString().orEmpty(),
                )
            }
        }
    }
}
