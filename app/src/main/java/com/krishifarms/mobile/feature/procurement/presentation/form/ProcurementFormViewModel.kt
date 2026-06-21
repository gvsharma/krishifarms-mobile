package com.krishifarms.mobile.feature.procurement.presentation.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.util.ProcurementCalculator
import com.krishifarms.mobile.feature.procurement.domain.repository.CreateProcurementInput
import com.krishifarms.mobile.feature.procurement.domain.repository.ProcurementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProcurementFormViewModel @Inject constructor(
    private val repository: ProcurementRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProcurementFormUiState())
    val uiState: StateFlow<ProcurementFormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeFarmerOptions().collect { farmers ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        farmers = farmers,
                    )
                }
            }
        }
    }

    fun onFarmerSelected(farmerId: String) {
        val farmer = _uiState.value.farmers.find { it.id == farmerId }
        _uiState.update {
            it.copy(
                selectedFarmerId = farmerId,
                village = farmer?.village ?: it.village,
                validationErrors = it.validationErrors - "farmer",
            )
        }
    }

    fun onCropSelected(crop: String) {
        _uiState.update {
            it.copy(crop = crop, validationErrors = it.validationErrors - "crop")
        }
    }

    fun onVillageChanged(value: String) {
        _uiState.update { it.copy(village = value, validationErrors = it.validationErrors - "village") }
    }

    fun onBagsChanged(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.update { it.copy(bags = value, validationErrors = it.validationErrors - "bags") }
        }
    }

    fun onWeightChanged(value: String) {
        if (isValidDecimal(value)) {
            _uiState.update {
                val next = it.copy(weight = value, validationErrors = it.validationErrors - "weight")
                next.copy(netAmount = calculateNetAmount(next))
            }
        }
    }

    fun onMoistureChanged(value: String) {
        if (isValidDecimal(value)) {
            _uiState.update {
                it.copy(moisture = value, validationErrors = it.validationErrors - "moisture")
            }
        }
    }

    fun onRateChanged(value: String) {
        if (isValidDecimal(value)) {
            _uiState.update {
                val next = it.copy(rate = value, validationErrors = it.validationErrors - "rate")
                next.copy(netAmount = calculateNetAmount(next))
            }
        }
    }

    fun onDeductionsChanged(value: String) {
        if (isValidDecimal(value)) {
            _uiState.update {
                val next = it.copy(deductions = value, validationErrors = it.validationErrors - "deductions")
                next.copy(netAmount = calculateNetAmount(next))
            }
        }
    }

    fun onImageSelected(path: String) {
        _uiState.update { it.copy(localImagePath = path) }
    }

    fun onBillSelected(path: String) {
        _uiState.update { it.copy(localBillPath = path) }
    }

    fun save() {
        val state = _uiState.value
        val errors = validate(state)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(validationErrors = errors) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = repository.createProcurement(
                CreateProcurementInput(
                    farmerId = state.selectedFarmerId!!,
                    crop = state.crop,
                    village = state.village.trim(),
                    bags = state.bags.toInt(),
                    weight = state.weight.toDouble(),
                    moisture = state.moisture.toDoubleOrNull() ?: 0.0,
                    rate = state.rate.toDouble(),
                    deductions = state.deductions.toDoubleOrNull() ?: 0.0,
                    netAmount = state.netAmount,
                    localImagePath = state.localImagePath,
                    localBillPath = state.localBillPath,
                ),
            )
            result.fold(
                onSuccess = { id ->
                    _uiState.update {
                        it.copy(isSaving = false, saveSuccess = true, savedProcurementId = id)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = error.message)
                    }
                },
            )
        }
    }

    private fun validate(state: ProcurementFormUiState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (state.selectedFarmerId.isNullOrBlank()) errors["farmer"] = "required"
        if (state.crop.isBlank()) errors["crop"] = "required"
        if (state.village.isBlank()) errors["village"] = "required"
        if (state.bags.isBlank() || state.bags.toIntOrNull() == null || state.bags.toInt() <= 0) {
            errors["bags"] = "required"
        }
        if (state.weight.isBlank() || state.weight.toDoubleOrNull() == null || state.weight.toDouble() <= 0) {
            errors["weight"] = "required"
        }
        if (state.rate.isBlank() || state.rate.toDoubleOrNull() == null || state.rate.toDouble() <= 0) {
            errors["rate"] = "required"
        }
        return errors
    }

    private fun calculateNetAmount(state: ProcurementFormUiState): Double {
        val weight = state.weight.toDoubleOrNull() ?: 0.0
        val rate = state.rate.toDoubleOrNull() ?: 0.0
        val deductions = state.deductions.toDoubleOrNull() ?: 0.0
        return ProcurementCalculator.netAmount(weight, rate, deductions)
    }

    private fun isValidDecimal(value: String): Boolean {
        if (value.isEmpty()) return true
        return value.matches(Regex("^\\d*(\\.\\d{0,2})?$"))
    }
}
