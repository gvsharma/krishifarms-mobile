package com.krishifarms.mobile.feature.expense.presentation.detail

import com.krishifarms.mobile.feature.expense.domain.model.Expense

data class ExpenseDetailUiState(
    val isLoading: Boolean = true,
    val expense: Expense? = null,
    val errorMessage: String? = null,
)
