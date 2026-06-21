package com.krishifarms.mobile.feature.expense.presentation.list

import com.krishifarms.mobile.feature.expense.domain.model.Expense
import com.krishifarms.mobile.feature.expense.domain.model.ExpenseCategory

data class ExpenseListUiState(
    val isLoading: Boolean = true,
    val expenses: List<Expense> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: ExpenseCategory? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val isDateFilterEnabled: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val pendingSyncCount: Int = 0,
    val errorMessage: String? = null,
)
