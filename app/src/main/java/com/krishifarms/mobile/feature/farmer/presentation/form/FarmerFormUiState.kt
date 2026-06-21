package com.krishifarms.mobile.feature.farmer.presentation.form

enum class FarmerFormFieldError {
    NAME_REQUIRED,
    VILLAGE_REQUIRED,
    PHONE_REQUIRED,
    PHONE_INVALID,
    BANK_DETAILS_REQUIRED,
    LAND_ACRES_INVALID,
    CROP_TYPES_REQUIRED,
}

data class FarmerFormUiState(
    val farmerId: String? = null,
    val name: String = "",
    val village: String = "",
    val phone: String = "",
    val bankDetails: String = "",
    val landAcres: String = "",
    val selectedCropTypes: Set<String> = emptySet(),
    val availableCropTypes: List<String> = emptyList(),
    val nameError: FarmerFormFieldError? = null,
    val villageError: FarmerFormFieldError? = null,
    val phoneError: FarmerFormFieldError? = null,
    val bankDetailsError: FarmerFormFieldError? = null,
    val landAcresError: FarmerFormFieldError? = null,
    val cropTypesError: FarmerFormFieldError? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
)
