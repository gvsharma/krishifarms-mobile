package com.krishifarms.mobile.feature.worker.presentation.workorder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krishifarms.mobile.R
import com.krishifarms.mobile.feature.worker.domain.model.ActivityType
import com.krishifarms.mobile.feature.worker.domain.model.WorkOrder
import com.krishifarms.mobile.feature.worker.presentation.components.CurrencyText
import com.krishifarms.mobile.feature.worker.presentation.components.DurationText
import com.krishifarms.mobile.feature.worker.presentation.components.SyncStatusBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderListScreen(
    onBack: () -> Unit,
    onWorkOrderClick: (String) -> Unit,
    onAddWorkOrder: () -> Unit,
    viewModel: WorkOrderListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.worker_work_orders_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddWorkOrder) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.worker_work_order_add))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            Column {
                WorkOrderFilters(uiState = uiState, viewModel = viewModel)
                if (uiState.workOrders.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.common_no_data))
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.workOrders, key = { it.id }) { order ->
                            WorkOrderListItem(order, dateFormat) { onWorkOrderClick(order.id) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkOrderFilters(
    uiState: WorkOrderListUiState,
    viewModel: WorkOrderListViewModel,
) {
    var workerExpanded by remember { mutableStateOf(false) }
    var activityExpanded by remember { mutableStateOf(false) }
    var farmExpanded by remember { mutableStateOf(false) }

    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        ExposedDropdownMenuBox(expanded = workerExpanded, onExpandedChange = { workerExpanded = it }) {
            OutlinedTextField(
                value = uiState.workers.find { it.id == uiState.filterWorkerId }?.name
                    ?: stringResource(R.string.worker_filter_all_workers),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.worker_name)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(workerExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded = workerExpanded, onDismissRequest = { workerExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.worker_filter_all_workers)) },
                    onClick = { viewModel.setWorkerFilter(null); workerExpanded = false },
                )
                uiState.workers.forEach { worker ->
                    DropdownMenuItem(
                        text = { Text(worker.name) },
                        onClick = { viewModel.setWorkerFilter(worker.id); workerExpanded = false },
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = activityExpanded,
            onExpandedChange = { activityExpanded = it },
            modifier = Modifier.padding(top = 8.dp),
        ) {
            OutlinedTextField(
                value = uiState.filterActivityType ?: stringResource(R.string.worker_filter_all_activities),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.worker_activity_type)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(activityExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded = activityExpanded, onDismissRequest = { activityExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.worker_filter_all_activities)) },
                    onClick = { viewModel.setActivityFilter(null); activityExpanded = false },
                )
                ActivityType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.label) },
                        onClick = { viewModel.setActivityFilter(type.label); activityExpanded = false },
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = farmExpanded,
            onExpandedChange = { farmExpanded = it },
            modifier = Modifier.padding(top = 8.dp),
        ) {
            OutlinedTextField(
                value = uiState.farms.find { it.id == uiState.filterFarmId }?.name
                    ?: stringResource(R.string.worker_filter_all_farms),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.worker_farm)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(farmExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded = farmExpanded, onDismissRequest = { farmExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.worker_filter_all_farms)) },
                    onClick = { viewModel.setFarmFilter(null); farmExpanded = false },
                )
                uiState.farms.forEach { farm ->
                    DropdownMenuItem(
                        text = { Text(farm.name) },
                        onClick = { viewModel.setFarmFilter(farm.id); farmExpanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkOrderListItem(
    order: WorkOrder,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit,
) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Text(order.workerName, style = MaterialTheme.typography.titleMedium)
            Text(order.activityType, style = MaterialTheme.typography.bodyMedium)
            Text(order.farmName, style = MaterialTheme.typography.bodySmall)
            Text(dateFormat.format(Date(order.startTime)), style = MaterialTheme.typography.bodySmall)
            DurationText(order.durationMinutes)
            CurrencyText(order.cost)
            SyncStatusBadge(order.syncStatus, Modifier.padding(top = 4.dp))
        }
    }
}
