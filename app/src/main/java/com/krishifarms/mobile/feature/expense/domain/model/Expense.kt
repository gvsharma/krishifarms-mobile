package com.krishifarms.mobile.feature.expense.domain.model

import com.krishifarms.mobile.core.common.SyncStatus

data class Expense(
    val id: String,
    val serverId: String?,
    val category: ExpenseCategory,
    val amount: Double,
    val description: String,
    val expenseDate: Long,
    val vendor: String?,
    val paymentMethod: PaymentMethod?,
    val localBillPath: String?,
    val remoteBillUrl: String?,
    val syncStatus: SyncStatus,
    val createdAt: Long,
)
