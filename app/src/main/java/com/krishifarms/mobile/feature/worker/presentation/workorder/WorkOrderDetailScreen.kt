package com.krishifarms.mobile.feature.worker.presentation.workorder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krishifarms.mobile.R
import com.krishifarms.mobile.feature.worker.presentation.components.CurrencyText
import com.krishifarms.mobile.feature.worker.presentation.components.DetailRow
import com.krishifarms.mobile.feature.worker.presentation.components.DurationText
import com.krishifarms.mobile.feature.worker.presentation.components.SyncStatusBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderDetailScreen(
    onBack: () -> Unit,
    viewModel: WorkOrderDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timeFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.worker_work_order_detail)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(Modifier.padding(padding).padding(32.dp))
        } else {
            val order = uiState.workOrder!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
            ) {
                DetailRow(stringResource(R.string.worker_name), order.workerName)
                DetailRow(stringResource(R.string.worker_activity_type), order.activityType)
                DetailRow(stringResource(R.string.worker_farm), order.farmName)
                DetailRow(stringResource(R.string.worker_start_time), timeFormat.format(Date(order.startTime)))
                DetailRow(stringResource(R.string.worker_end_time), timeFormat.format(Date(order.endTime)))
                DetailRow(stringResource(R.string.worker_duration)) {
                    DurationText(order.durationMinutes)
                }
                DetailRow(stringResource(R.string.worker_hourly_rate), "₹%.2f".format(order.hourlyRate))
                DetailRow(stringResource(R.string.worker_cost)) {
                    CurrencyText(order.cost)
                }
                SyncStatusBadge(order.syncStatus, Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    com.krishifarms.mobile.feature.worker.presentation.components.DetailRow(label = label, value = value)
}

@Composable
private fun DetailRow(label: String, valueContent: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
    ) {
        Text(label)
        valueContent()
    }
}
