package com.krishifarms.mobile.feature.expense.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.navigation.ExpenseRoutes
import com.krishifarms.mobile.feature.expense.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ExpenseRepository,
) : ViewModel() {

    private val expenseId: String = checkNotNull(savedStateHandle[ExpenseRoutes.ARG_EXPENSE_ID])

    private val _uiState = MutableStateFlow(ExpenseDetailUiState())
    val uiState: StateFlow<ExpenseDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeExpense(expenseId)
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message,
                        )
                    }
                }
                .collect { expense ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            expense = expense,
                            errorMessage = if (expense == null) "Expense not found" else null,
                        )
                    }
                }
        }
    }
}
