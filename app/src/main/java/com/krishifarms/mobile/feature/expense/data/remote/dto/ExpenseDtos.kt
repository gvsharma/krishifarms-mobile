package com.krishifarms.mobile.feature.expense.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExpenseDto(
    val id: String,
    val category: String,
    val amount: Double,
    val description: String,
    @SerialName("expense_date")
    val expenseDate: Long,
    val vendor: String? = null,
    @SerialName("payment_method")
    val paymentMethod: String? = null,
    @SerialName("bill_url")
    val billUrl: String? = null,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("updated_at")
    val updatedAt: Long,
)

@Serializable
data class CreateExpenseRequest(
    @SerialName("local_id")
    val localId: String,
    val category: String,
    val amount: Double,
    val description: String,
    @SerialName("expense_date")
    val expenseDate: Long,
    val vendor: String? = null,
    @SerialName("payment_method")
    val paymentMethod: String? = null,
)

@Serializable
data class CreateExpenseResponse(
    val expense: ExpenseDto,
)
