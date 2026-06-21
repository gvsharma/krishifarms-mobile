package com.krishifarms.mobile.feature.farmer.presentation.list

import com.krishifarms.mobile.feature.farmer.domain.model.Farmer

data class FarmerListUiState(
    val farmers: List<Farmer> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val canCreate: Boolean = false,
)
