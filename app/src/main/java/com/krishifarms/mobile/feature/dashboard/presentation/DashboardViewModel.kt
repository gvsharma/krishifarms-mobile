package com.krishifarms.mobile.feature.dashboard.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.common.Resource
import com.krishifarms.mobile.R
import com.krishifarms.mobile.core.security.session.SessionManager
import com.krishifarms.mobile.core.security.rbac.DashboardCardAccess
import com.krishifarms.mobile.feature.dashboard.domain.model.DashboardMetric
import com.krishifarms.mobile.feature.dashboard.domain.model.DashboardSummary
import com.krishifarms.mobile.feature.dashboard.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DashboardCardType {
    TODAYS_PROCUREMENT,
    TODAYS_EXPENSES,
    TODAYS_COLLECTIONS,
    PENDING_FARMER_PAYMENTS,
    PENDING_COLLECTIONS,
    WORKER_ATTENDANCE,
    ACTIVE_RENTALS,
    ;

    val labelRes: Int
        @StringRes
        get() = when (this) {
            TODAYS_PROCUREMENT -> R.string.dashboard_card_todays_procurement
            TODAYS_EXPENSES -> R.string.dashboard_card_todays_expenses
            TODAYS_COLLECTIONS -> R.string.dashboard_card_todays_collections
            PENDING_FARMER_PAYMENTS -> R.string.dashboard_card_pending_farmer_payments
            PENDING_COLLECTIONS -> R.string.dashboard_card_pending_collections
            WORKER_ATTENDANCE -> R.string.dashboard_card_worker_attendance
            ACTIVE_RENTALS -> R.string.dashboard_card_active_rentals
        }
}

data class DashboardCardUiModel(
    val type: DashboardCardType,
    val metric: DashboardMetric,
)

sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Success(
        val cards: List<DashboardCardUiModel>,
        val lastUpdatedAt: Long,
        val isFromCache: Boolean,
        val isRefreshing: Boolean = false,
    ) : DashboardUiState

    data class Error(
        val message: String,
        val cachedCards: List<DashboardCardUiModel>? = null,
        val lastUpdatedAt: Long? = null,
    ) : DashboardUiState

    data object Empty : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        dashboardRepository.observeSummary()
            .onEach { summary ->
                if (summary != null && _uiState.value is DashboardUiState.Loading) {
                    _uiState.value = summary.toSuccessUiState()
                }
            }
            .launchIn(viewModelScope)

        loadDashboard()
    }

    fun refresh() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is DashboardUiState.Success) {
                _uiState.update { current.copy(isRefreshing = true) }
            }

            when (val result = dashboardRepository.refresh(forceRemote = true)) {
                is Resource.Success -> {
                    _uiState.value = result.data.toSuccessUiState(isRefreshing = false)
                }

                is Resource.Error -> {
                    val cached = (current as? DashboardUiState.Success)?.cards
                    _uiState.value = DashboardUiState.Error(
                        message = result.message,
                        cachedCards = cached,
                        lastUpdatedAt = (current as? DashboardUiState.Success)?.lastUpdatedAt,
                    )
                }

                is Resource.Loading -> Unit
            }
        }
    }

    fun retry() {
        loadDashboard(forceRemote = true)
    }

    private fun loadDashboard(forceRemote: Boolean = false) {
        viewModelScope.launch {
            if (_uiState.value !is DashboardUiState.Success) {
                _uiState.value = DashboardUiState.Loading
            }

            when (val result = dashboardRepository.refresh(forceRemote = forceRemote)) {
                is Resource.Success -> {
                    _uiState.value = if (result.data.isEmptySummary()) {
                        DashboardUiState.Empty
                    } else {
                        result.data.toSuccessUiState()
                    }
                }

                is Resource.Error -> {
                    _uiState.value = DashboardUiState.Error(message = result.message)
                }

                is Resource.Loading -> Unit
            }
        }
    }

    private fun DashboardSummary.toSuccessUiState(isRefreshing: Boolean = false): DashboardUiState.Success {
        return DashboardUiState.Success(
            cards = toCards(),
            lastUpdatedAt = lastUpdatedAt,
            isFromCache = isFromCache,
            isRefreshing = isRefreshing,
        )
    }

    private fun DashboardSummary.toCards(): List<DashboardCardUiModel> {
        val visible = DashboardCardAccess.visibleCards(sessionManager.session.value)
        return listOf(
            DashboardCardUiModel(DashboardCardType.TODAYS_PROCUREMENT, todaysProcurement),
            DashboardCardUiModel(DashboardCardType.TODAYS_EXPENSES, todaysExpenses),
            DashboardCardUiModel(DashboardCardType.TODAYS_COLLECTIONS, todaysCollections),
            DashboardCardUiModel(DashboardCardType.PENDING_FARMER_PAYMENTS, pendingFarmerPayments),
            DashboardCardUiModel(DashboardCardType.PENDING_COLLECTIONS, pendingCollections),
            DashboardCardUiModel(DashboardCardType.WORKER_ATTENDANCE, workerAttendance),
            DashboardCardUiModel(DashboardCardType.ACTIVE_RENTALS, activeRentals),
        ).filter { it.type in visible }
    }

    private fun DashboardSummary.isEmptySummary(): Boolean {
        return listOf(
            todaysProcurement,
            todaysExpenses,
            todaysCollections,
            pendingFarmerPayments,
            pendingCollections,
            workerAttendance,
            activeRentals,
        ).all { metric ->
            metric.count == 0 && (metric.amount == null || metric.amount == 0.0)
        }
    }
}
