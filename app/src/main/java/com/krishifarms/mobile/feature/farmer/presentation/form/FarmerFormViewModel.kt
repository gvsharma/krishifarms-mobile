package com.krishifarms.mobile.feature.farmer.presentation.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.common.Constants
import com.krishifarms.mobile.core.common.Resource
import com.krishifarms.mobile.core.navigation.Routes
import com.krishifarms.mobile.feature.farmer.domain.model.AvailableCropTypes
import com.krishifarms.mobile.feature.farmer.domain.repository.FarmerInput
import com.krishifarms.mobile.feature.farmer.domain.repository.FarmerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FarmerFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FarmerRepository,
) : ViewModel() {

    private val farmerId: String? = savedStateHandle.get<String>(Routes.FARMER_ID_ARG)

    private val _uiState = MutableStateFlow(
        FarmerFormUiState(
            farmerId = farmerId,
            availableCropTypes = AvailableCropTypes.ALL,
        ),
    )
    val uiState: StateFlow<FarmerFormUiState> = _uiState.asStateFlow()

    init {
        farmerId?.let { loadFarmer(it) }
    }

    private fun loadFarmer(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val farmer = repository.getFarmerById(id).first()
            if (farmer != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = farmer.name,
                        village = farmer.village,
                        phone = farmer.phone,
                        bankDetails = farmer.bankDetails,
                        landAcres = farmer.landAcres.toString(),
                        selectedCropTypes = farmer.cropTypes.toSet(),
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Farmer not found")
                }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null) }
    fun onVillageChange(value: String) = _uiState.update { it.copy(village = value, villageError = null) }
    fun onPhoneChange(value: String) = _uiState.update {
        it.copy(phone = value.filter(Char::isDigit).take(Constants.MOBILE_NUMBER_LENGTH), phoneError = null)
    }
    fun onBankDetailsChange(value: String) = _uiState.update { it.copy(bankDetails = value, bankDetailsError = null) }
    fun onLandAcresChange(value: String) = _uiState.update {
        it.copy(landAcres = value.filter { char -> char.isDigit() || char == '.' }, landAcresError = null)
    }

    fun onCropTypeToggle(cropType: String) {
        _uiState.update { state ->
            val updated = state.selectedCropTypes.toMutableSet()
            if (updated.contains(cropType)) updated.remove(cropType) else updated.add(cropType)
            state.copy(selectedCropTypes = updated, cropTypesError = null)
        }
    }

    fun save() {
        val state = _uiState.value
        val validation = validate(state)
        if (validation != null) {
            _uiState.update { validation }
            return
        }

        val input = FarmerInput(
            name = state.name.trim(),
            village = state.village.trim(),
            phone = state.phone.trim(),
            bankDetails = state.bankDetails.trim(),
            landAcres = state.landAcres.toDouble(),
            cropTypes = state.selectedCropTypes.toList(),
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = if (state.farmerId == null) {
                repository.createFarmer(input)
            } else {
                repository.updateFarmer(state.farmerId, input)
            }
            when (result) {
                is Resource.Success -> _uiState.update { it.copy(isSaving = false, isSaved = true) }
                is Resource.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun validate(state: FarmerFormUiState): FarmerFormUiState? {
        var updated = state
        var hasError = false

        if (state.name.isBlank()) {
            updated = updated.copy(nameError = FarmerFormFieldError.NAME_REQUIRED)
            hasError = true
        }
        if (state.village.isBlank()) {
            updated = updated.copy(villageError = FarmerFormFieldError.VILLAGE_REQUIRED)
            hasError = true
        }
        if (state.phone.isBlank()) {
            updated = updated.copy(phoneError = FarmerFormFieldError.PHONE_REQUIRED)
            hasError = true
        } else if (state.phone.length != Constants.MOBILE_NUMBER_LENGTH) {
            updated = updated.copy(phoneError = FarmerFormFieldError.PHONE_INVALID)
            hasError = true
        }
        if (state.bankDetails.isBlank()) {
            updated = updated.copy(bankDetailsError = FarmerFormFieldError.BANK_DETAILS_REQUIRED)
            hasError = true
        }
        val acres = state.landAcres.toDoubleOrNull()
        if (acres == null || acres <= 0.0) {
            updated = updated.copy(landAcresError = FarmerFormFieldError.LAND_ACRES_INVALID)
            hasError = true
        }
        if (state.selectedCropTypes.isEmpty()) {
            updated = updated.copy(cropTypesError = FarmerFormFieldError.CROP_TYPES_REQUIRED)
            hasError = true
        }

        return if (hasError) updated else null
    }
}
