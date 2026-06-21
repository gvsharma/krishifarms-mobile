package com.krishifarms.mobile.feature.worker.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishifarms.mobile.core.common.Resource
import com.krishifarms.mobile.core.security.rbac.ActionPermissions
import com.krishifarms.mobile.core.security.rbac.Permission
import com.krishifarms.mobile.core.security.rbac.PermissionManager
import com.krishifarms.mobile.feature.worker.domain.model.Worker
import com.krishifarms.mobile.feature.worker.domain.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkerListUiState(
    val workers: List<Worker> = emptyList(),
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val canCreate: Boolean = false,
    val canViewAttendance: Boolean = false,
    val canViewWorkOrders: Boolean = false,
)

@HiltViewModel
class WorkerListViewModel @Inject constructor(
    private val workerRepository: WorkerRepository,
    permissionManager: PermissionManager,
) : ViewModel() {

    private val actions = ActionPermissions.from(permissionManager)
    private val canViewAttendance = permissionManager.has(Permission.ATTENDANCE_VIEW)
    private val canViewWorkOrders = permissionManager.has(Permission.WORK_ORDER_VIEW)

    private val searchQuery = MutableStateFlow("")
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<WorkerListUiState> = combine(
        searchQuery.flatMapLatest { query -> workerRepository.observeWorkers(query) },
        searchQuery,
        isRefreshing,
        errorMessage,
    ) { workers, query, refreshing, error ->
        WorkerListUiState(
            workers = workers,
            searchQuery = query,
            isRefreshing = refreshing,
            errorMessage = error,
            canCreate = actions.worker.canCreate,
            canViewAttendance = canViewAttendance,
            canViewWorkOrders = canViewWorkOrders,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        WorkerListUiState(
            canCreate = actions.worker.canCreate,
            canViewAttendance = canViewAttendance,
            canViewWorkOrders = canViewWorkOrders,
        ),
    )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            errorMessage.value = null
            when (val result = workerRepository.syncWorkers()) {
                is Resource.Error -> errorMessage.value = result.message
                else -> Unit
            }
            isRefreshing.value = false
        }
    }
}
