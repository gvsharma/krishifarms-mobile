package com.krishifarms.mobile.feature.farmer.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.navigation.Routes
import com.krishifarms.mobile.core.security.rbac.ActionPermissions
import com.krishifarms.mobile.core.security.rbac.PermissionManager
import com.krishifarms.mobile.feature.farmer.domain.repository.FarmerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FarmerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FarmerRepository,
    permissionManager: PermissionManager,
) : ViewModel() {

    private val farmerId: String = checkNotNull(savedStateHandle[Routes.FARMER_ID_ARG])
    private val canEdit = ActionPermissions.from(permissionManager).farmer.canUpdate

    private val _uiState = MutableStateFlow(FarmerDetailUiState(canEdit = canEdit))
    val uiState: StateFlow<FarmerDetailUiState> = _uiState.asStateFlow()

    init {
        observeFarmer()
    }

    private fun observeFarmer() {
        viewModelScope.launch {
            repository.getFarmerById(farmerId)
                .catch { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
                .collect { farmer ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            farmer = farmer,
                            errorMessage = if (farmer == null) "Farmer not found" else null,
                        )
                    }
                }
        }
    }
}
