package com.krishifarms.mobile.feature.expense.presentation.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.util.AttachmentStorage
import com.krishifarms.mobile.feature.expense.domain.model.ExpenseCategory
import com.krishifarms.mobile.feature.expense.domain.model.PaymentMethod
import com.krishifarms.mobile.feature.expense.domain.repository.CreateExpenseInput
import com.krishifarms.mobile.feature.expense.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseFormViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    val attachmentStorage: AttachmentStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseFormUiState())
    val uiState: StateFlow<ExpenseFormUiState> = _uiState.asStateFlow()

    fun onCategoryChanged(category: ExpenseCategory) {
        _uiState.update {
            it.copy(category = category, validationErrors = it.validationErrors - "category")
        }
    }

    fun onAmountChanged(value: String) {
        if (isValidDecimal(value)) {
            _uiState.update {
                it.copy(amount = value, validationErrors = it.validationErrors - "amount")
            }
        }
    }

    fun onDescriptionChanged(value: String) {
        _uiState.update {
            it.copy(description = value, validationErrors = it.validationErrors - "description")
        }
    }

    fun onExpenseDateChanged(dateMillis: Long) {
        _uiState.update { it.copy(expenseDate = dateMillis) }
    }

    fun onVendorChanged(value: String) {
        _uiState.update { it.copy(vendor = value) }
    }

    fun onPaymentMethodChanged(method: PaymentMethod?) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun onBillSelected(path: String) {
        _uiState.update { it.copy(localBillPath = path) }
    }

    fun onBillCleared() {
        _uiState.update { it.copy(localBillPath = null) }
    }

    fun save() {
        val state = _uiState.value
        val errors = validate(state)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(validationErrors = errors) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = repository.createExpense(
                CreateExpenseInput(
                    category = state.category,
                    amount = state.amount.toDouble(),
                    description = state.description.trim(),
                    expenseDate = state.expenseDate,
                    vendor = state.vendor.trim().takeIf { it.isNotBlank() },
                    paymentMethod = state.paymentMethod,
                    localBillPath = state.localBillPath,
                ),
            )
            result.fold(
                onSuccess = { id ->
                    _uiState.update {
                        it.copy(isSaving = false, saveSuccess = true, savedExpenseId = id)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = error.message)
                    }
                },
            )
        }
    }

    private fun validate(state: ExpenseFormUiState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (state.amount.isBlank() || state.amount.toDoubleOrNull() == null || state.amount.toDouble() <= 0) {
            errors["amount"] = "required"
        }
        if (state.description.isBlank()) {
            errors["description"] = "required"
        }
        return errors
    }

    private fun isValidDecimal(value: String): Boolean {
        if (value.isEmpty()) return true
        return value.matches(Regex("^\\d*(\\.\\d{0,2})?$"))
    }
}
