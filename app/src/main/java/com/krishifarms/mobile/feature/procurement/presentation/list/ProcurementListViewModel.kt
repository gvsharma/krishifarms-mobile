package com.krishifarms.mobile.feature.procurement.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.common.NetworkMonitor
import com.krishifarms.mobile.feature.procurement.domain.repository.ProcurementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProcurementListViewModel @Inject constructor(
    private val repository: ProcurementRepository,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProcurementListUiState())
    val uiState: StateFlow<ProcurementListUiState> = _uiState.asStateFlow()

    init {
        observeProcurements()
    }

    private fun observeProcurements() {
        viewModelScope.launch {
            repository.observeProcurements()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message,
                        )
                    }
                }
                .collect { procurements ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            procurements = procurements,
                            errorMessage = null,
                            isOffline = !networkMonitor.isOnline(),
                        )
                    }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, isOffline = !networkMonitor.isOnline()) }
            runCatching {
                repository.refreshFromServer()
                repository.syncPending()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}
