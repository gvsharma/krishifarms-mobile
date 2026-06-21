package com.krishifarms.mobile.feature.worker.presentation.list

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krishifarms.mobile.R
import com.krishifarms.mobile.feature.worker.domain.model.Worker
import com.krishifarms.mobile.feature.worker.presentation.components.CurrencyText
import com.krishifarms.mobile.feature.worker.presentation.components.SyncStatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerListScreen(
    onWorkerClick: (String) -> Unit,
    onAddWorker: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToWorkOrders: () -> Unit,
    viewModel: WorkerListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.workers_title)) },
                actions = {
                    if (uiState.canViewAttendance) {
                        IconButton(onClick = onNavigateToAttendance) {
                            Text(stringResource(R.string.worker_attendance_short), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    if (uiState.canViewWorkOrders) {
                        IconButton(onClick = onNavigateToWorkOrders) {
                            Text(stringResource(R.string.worker_work_orders_short), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    IconButton(onClick = { /* search handled inline */ }) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.common_search))
                    }
                },
            )
        },
        floatingActionButton = {
            if (uiState.canCreate) {
                FloatingActionButton(onClick = onAddWorker) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.worker_add))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (uiState.workers.isEmpty() && !uiState.isRefreshing) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.common_no_data))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.workers, key = { it.id }) { worker ->
                        WorkerListItem(worker = worker, onClick = { onWorkerClick(worker.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkerListItem(
    worker: Worker,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(worker.name, style = MaterialTheme.typography.titleMedium)
            worker.phone?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            CurrencyText(
                amount = worker.defaultHourlyRate,
                modifier = Modifier.padding(top = 4.dp),
            )
            SyncStatusBadge(syncStatus = worker.syncStatus, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
