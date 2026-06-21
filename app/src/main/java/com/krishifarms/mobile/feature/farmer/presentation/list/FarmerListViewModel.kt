package com.krishifarms.mobile.feature.farmer.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.common.NetworkMonitor
import com.krishifarms.mobile.feature.farmer.domain.repository.FarmerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FarmerListViewModel @Inject constructor(
    private val repository: FarmerRepository,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmerListUiState())
    val uiState: StateFlow<FarmerListUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        observeFarmers()
    }

    private fun observeFarmers() {
        viewModelScope.launch {
            searchQuery
                .flatMapLatest { query -> repository.getFarmers(query) }
                .catch { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
                .collect { farmers ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            farmers = farmers,
                            errorMessage = null,
                            isOffline = !networkMonitor.isOnline(),
                        )
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, isOffline = !networkMonitor.isOnline()) }
            repository.syncFarmers().let { result ->
                if (result is com.krishifarms.mobile.core.common.Resource.Error) {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}
