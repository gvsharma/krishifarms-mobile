package com.krishifarms.mobile.feature.worker.presentation.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krishifarms.mobile.R
import com.krishifarms.mobile.core.database.entity.AttendanceStatus
import com.krishifarms.mobile.feature.worker.domain.model.Attendance
import com.krishifarms.mobile.feature.worker.domain.model.WorkOrder
import com.krishifarms.mobile.feature.worker.presentation.components.CurrencyText
import com.krishifarms.mobile.feature.worker.presentation.components.DetailRow
import com.krishifarms.mobile.feature.worker.presentation.components.DurationText
import com.krishifarms.mobile.feature.worker.presentation.components.SyncStatusBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onWorkOrderClick: (String) -> Unit,
    viewModel: WorkerDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dateFormat = rememberDateFormat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.worker?.name ?: stringResource(R.string.workers_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    uiState.worker?.let { worker ->
                        IconButton(onClick = { onEdit(worker.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.worker_edit))
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(Modifier.padding(padding).padding(32.dp))
        } else {
            Column(Modifier.fillMaxSize().padding(padding)) {
                TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
                    WorkerDetailTab.entries.forEach { tab ->
                        Tab(
                            selected = uiState.selectedTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            text = {
                                Text(
                                    when (tab) {
                                        WorkerDetailTab.INFO -> stringResource(R.string.worker_tab_info)
                                        WorkerDetailTab.ATTENDANCE -> stringResource(R.string.worker_tab_attendance)
                                        WorkerDetailTab.WORK_ORDERS -> stringResource(R.string.worker_tab_work_orders)
                                    },
                                )
                            },
                        )
                    }
                }

                when (uiState.selectedTab) {
                    WorkerDetailTab.INFO -> WorkerInfoTab(uiState.worker!!)
                    WorkerDetailTab.ATTENDANCE -> AttendanceHistoryTab(uiState.attendance, dateFormat)
                    WorkerDetailTab.WORK_ORDERS -> WorkOrderHistoryTab(uiState.workOrders, dateFormat, onWorkOrderClick)
                }
            }
        }
    }
}

@Composable
private fun WorkerInfoTab(worker: com.krishifarms.mobile.feature.worker.domain.model.Worker) {
    Column(Modifier.padding(16.dp)) {
        DetailRow(stringResource(R.string.worker_name), worker.name)
        worker.phone?.let { DetailRow(stringResource(R.string.worker_phone), it) }
        DetailRow(stringResource(R.string.worker_hourly_rate), "₹%.2f".format(worker.defaultHourlyRate))
        SyncStatusBadge(syncStatus = worker.syncStatus, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun AttendanceHistoryTab(attendance: List<Attendance>, dateFormat: SimpleDateFormat) {
    if (attendance.isEmpty()) {
        Text(stringResource(R.string.common_no_data), Modifier.padding(16.dp))
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(attendance, key = { it.id.ifBlank { "${it.workerId}_${it.date}" } }) { record ->
            Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(dateFormat.format(Date(record.date)), style = MaterialTheme.typography.titleSmall)
                    Text(attendanceStatusLabel(record.status))
                    SyncStatusBadge(record.syncStatus)
                }
            }
        }
    }
}

@Composable
private fun WorkOrderHistoryTab(
    workOrders: List<WorkOrder>,
    dateFormat: SimpleDateFormat,
    onWorkOrderClick: (String) -> Unit,
) {
    if (workOrders.isEmpty()) {
        Text(stringResource(R.string.common_no_data), Modifier.padding(16.dp))
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(workOrders, key = { it.id }) { order ->
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { onWorkOrderClick(order.id) },
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(order.activityType, style = MaterialTheme.typography.titleSmall)
                    Text(order.farmName, style = MaterialTheme.typography.bodySmall)
                    Text(dateFormat.format(Date(order.startTime)))
                    DurationText(order.durationMinutes)
                    CurrencyText(order.cost)
                }
            }
        }
    }
}

@Composable
private fun attendanceStatusLabel(status: AttendanceStatus): String = when (status) {
    AttendanceStatus.PRESENT -> stringResource(R.string.worker_attendance_present)
    AttendanceStatus.ABSENT -> stringResource(R.string.worker_attendance_absent)
    AttendanceStatus.HALF_DAY -> stringResource(R.string.worker_attendance_half_day)
}

@Composable
private fun rememberDateFormat(): SimpleDateFormat =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
