package com.krishifarms.mobile.feature.procurement.presentation.detail

import com.krishifarms.mobile.feature.procurement.domain.model.Procurement

data class ProcurementDetailUiState(
    val isLoading: Boolean = true,
    val procurement: Procurement? = null,
    val errorMessage: String? = null,
)
