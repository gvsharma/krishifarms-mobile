package com.krishifarms.mobile.feature.procurement.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.krishifarms.mobile.R
import com.krishifarms.mobile.core.ui.components.SyncStatusIcon
import com.krishifarms.mobile.core.ui.components.syncStatusLabel
import com.krishifarms.mobile.feature.procurement.domain.model.Procurement
import java.io.File
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcurementDetailScreen(
    onBack: () -> Unit,
    viewModel: ProcurementDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.procurement_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.procurement == null -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(uiState.errorMessage ?: stringResource(R.string.common_error))
                }
            }
            else -> {
                ProcurementDetailContent(
                    procurement = uiState.procurement,
                    currencyFormat = currencyFormat,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun ProcurementDetailContent(
    procurement: Procurement,
    currencyFormat: NumberFormat,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RowWithSyncStatus(
            label = stringResource(R.string.procurement_field_farmer),
            value = procurement.farmerName ?: procurement.farmerId,
            syncStatus = procurement.syncStatus,
        )
        DetailField(stringResource(R.string.procurement_field_crop), procurement.crop)
        DetailField(stringResource(R.string.procurement_field_village), procurement.village)
        DetailField(stringResource(R.string.procurement_field_bags), procurement.bags.toString())
        DetailField(stringResource(R.string.procurement_field_weight), "${procurement.weight} kg")
        DetailField(stringResource(R.string.procurement_field_moisture), "${procurement.moisture}%")
        DetailField(stringResource(R.string.procurement_field_rate), currencyFormat.format(procurement.rate))
        DetailField(stringResource(R.string.procurement_field_deductions), currencyFormat.format(procurement.deductions))
        DetailField(
            stringResource(R.string.procurement_field_net_amount),
            currencyFormat.format(procurement.netAmount),
            emphasized = true,
        )

        AttachmentSection(
            title = stringResource(R.string.procurement_field_image),
            localPath = procurement.localImagePath,
            remoteUrl = procurement.remoteImageUrl,
        )
        AttachmentSection(
            title = stringResource(R.string.procurement_field_bill),
            localPath = procurement.localBillPath,
            remoteUrl = procurement.remoteBillUrl,
        )
    }
}

@Composable
private fun RowWithSyncStatus(
    label: String,
    value: String,
    syncStatus: com.krishifarms.mobile.core.common.SyncStatus,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                SyncStatusIcon(status = syncStatus)
                Spacer(Modifier.padding(4.dp))
                Text(syncStatusLabel(syncStatus), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String,
    emphasized: Boolean = false,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                style = if (emphasized) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal,
                color = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun AttachmentSection(
    title: String,
    localPath: String?,
    remoteUrl: String?,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            val model = when {
                !localPath.isNullOrBlank() && File(localPath).exists() -> localPath
                !remoteUrl.isNullOrBlank() -> remoteUrl
                else -> null
            }
            if (model != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(model)
                        .crossfade(true)
                        .build(),
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    stringResource(R.string.procurement_no_attachment),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
