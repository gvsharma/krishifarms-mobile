package com.krishifarms.mobile.feature.expense.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.common.NetworkMonitor
import com.krishifarms.mobile.feature.expense.domain.model.ExpenseCategory
import com.krishifarms.mobile.feature.expense.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseListUiState())
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<ExpenseCategory?>(null)
    private val startDate = MutableStateFlow<Long?>(null)
    private val endDate = MutableStateFlow<Long?>(null)

    init {
        observeExpenses()
        observePendingCount()
    }

    private fun observeExpenses() {
        viewModelScope.launch {
            combine(searchQuery, selectedCategory, startDate, endDate) { query, category, start, end ->
                repository.observeExpenses(
                    searchQuery = query,
                    category = category,
                    startDate = start,
                    endDate = end,
                )
            }
                .flatMapLatest { it }
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message,
                        )
                    }
                }
                .collect { expenses ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            expenses = expenses,
                            searchQuery = searchQuery.value,
                            selectedCategory = selectedCategory.value,
                            startDate = startDate.value,
                            endDate = endDate.value,
                            errorMessage = null,
                            isOffline = !networkMonitor.isOnline(),
                        )
                    }
                }
        }
    }

    private fun observePendingCount() {
        viewModelScope.launch {
            repository.observePendingCount().collect { count ->
                _uiState.update { it.copy(pendingSyncCount = count) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCategorySelected(category: ExpenseCategory?) {
        selectedCategory.value = category
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onDateRangeChanged(start: Long?, end: Long?, enabled: Boolean) {
        startDate.value = if (enabled) start else null
        endDate.value = if (enabled) end else null
        _uiState.update {
            it.copy(
                startDate = startDate.value,
                endDate = endDate.value,
                isDateFilterEnabled = enabled,
            )
        }
    }

    fun clearFilters() {
        searchQuery.value = ""
        selectedCategory.value = null
        startDate.value = null
        endDate.value = null
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedCategory = null,
                startDate = null,
                endDate = null,
                isDateFilterEnabled = false,
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, isOffline = !networkMonitor.isOnline()) }
            runCatching {
                repository.refreshFromServer()
                repository.syncPending()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}
