package com.krishifarms.mobile.feature.expense.presentation.form

import com.krishifarms.mobile.feature.expense.domain.model.ExpenseCategory
import com.krishifarms.mobile.feature.expense.domain.model.PaymentMethod

data class ExpenseFormUiState(
    val category: ExpenseCategory = ExpenseCategory.MISCELLANEOUS,
    val amount: String = "",
    val description: String = "",
    val expenseDate: Long = System.currentTimeMillis(),
    val vendor: String = "",
    val paymentMethod: PaymentMethod? = null,
    val localBillPath: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val savedExpenseId: String? = null,
    val errorMessage: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
)
