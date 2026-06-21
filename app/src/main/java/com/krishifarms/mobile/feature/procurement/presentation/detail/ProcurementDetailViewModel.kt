package com.krishifarms.mobile.feature.procurement.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.navigation.ProcurementRoutes
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
class ProcurementDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ProcurementRepository,
) : ViewModel() {

    private val procurementId: String = checkNotNull(savedStateHandle[ProcurementRoutes.ARG_PROCUREMENT_ID])

    private val _uiState = MutableStateFlow(ProcurementDetailUiState())
    val uiState: StateFlow<ProcurementDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProcurement(procurementId)
                .catch { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
                .collect { procurement ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            procurement = procurement,
                            errorMessage = if (procurement == null) "Procurement not found" else null,
                        )
                    }
                }
        }
    }
}
