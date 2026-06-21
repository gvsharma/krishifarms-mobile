package com.krishifarms.mobile.feature.farmer.presentation.detail

import com.krishifarms.mobile.feature.farmer.domain.model.Farmer

data class FarmerDetailUiState(
    val farmer: Farmer? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val canEdit: Boolean = false,
)
