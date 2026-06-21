package com.krishifarms.mobile.feature.procurement.presentation.list

import com.krishifarms.mobile.feature.procurement.domain.model.Procurement

data class ProcurementListUiState(
    val isLoading: Boolean = true,
    val procurements: List<Procurement> = emptyList(),
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
)
