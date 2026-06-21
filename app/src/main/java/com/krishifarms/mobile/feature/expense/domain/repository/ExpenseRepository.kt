package com.krishifarms.mobile.feature.expense.domain.repository

import com.krishifarms.mobile.feature.expense.domain.model.Expense
import com.krishifarms.mobile.feature.expense.domain.model.ExpenseCategory
import com.krishifarms.mobile.feature.expense.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

data class CreateExpenseInput(
    val category: ExpenseCategory,
    val amount: Double,
    val description: String,
    val expenseDate: Long,
    val vendor: String?,
    val paymentMethod: PaymentMethod?,
    val localBillPath: String?,
)

interface ExpenseRepository {
    fun observeExpenses(
        searchQuery: String = "",
        category: ExpenseCategory? = null,
        startDate: Long? = null,
        endDate: Long? = null,
    ): Flow<List<Expense>>

    fun observeExpense(id: String): Flow<Expense?>
    fun observePendingCount(): Flow<Int>

    suspend fun createExpense(input: CreateExpenseInput): Result<String>
    suspend fun refreshFromServer()
    suspend fun syncPending()
}
