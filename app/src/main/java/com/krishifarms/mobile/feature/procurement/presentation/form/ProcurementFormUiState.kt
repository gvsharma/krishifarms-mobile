package com.krishifarms.mobile.feature.procurement.presentation.form

import com.krishifarms.mobile.feature.procurement.domain.model.FarmerOption

data class ProcurementFormUiState(
    val isLoading: Boolean = true,
    val farmers: List<FarmerOption> = emptyList(),
    val selectedFarmerId: String? = null,
    val crop: String = "",
    val village: String = "",
    val bags: String = "",
    val weight: String = "",
    val moisture: String = "",
    val rate: String = "",
    val deductions: String = "",
    val netAmount: Double = 0.0,
    val localImagePath: String? = null,
    val localBillPath: String? = null,
    val cropOptions: List<String> = DEFAULT_CROPS,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val savedProcurementId: String? = null,
    val errorMessage: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
) {
    val selectedFarmer: FarmerOption?
        get() = farmers.find { it.id == selectedFarmerId }

    companion object {
        val DEFAULT_CROPS = listOf(
            "Paddy",
            "Cotton",
            "Maize",
            "Chilli",
            "Turmeric",
            "Groundnut",
            "Soybean",
            "Sunflower",
        )
    }
}
