package com.krishifarms.mobile.feature.worker.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.krishifarms.mobile.R
import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.common.isPending

@Composable
fun SyncStatusBadge(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier,
) {
    if (!syncStatus.isPending() && syncStatus != SyncStatus.SYNC_FAILED) return

    val (label, containerColor) = when (syncStatus) {
        SyncStatus.PENDING_CREATE,
        SyncStatus.PENDING_UPDATE,
        SyncStatus.PENDING_DELETE,
        -> stringResource(R.string.common_sync_status_pending) to MaterialTheme.colorScheme.tertiaryContainer

        SyncStatus.SYNC_FAILED ->
            stringResource(R.string.common_sync_status_failed) to MaterialTheme.colorScheme.errorContainer

        SyncStatus.SYNCED -> return
    }

    AssistChip(
        onClick = {},
        enabled = false,
        modifier = modifier,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = AssistChipDefaults.assistChipColors(containerColor = containerColor),
    )
}

@Composable
fun CurrencyText(
    amount: Double,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "₹%.2f".format(amount),
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
fun DurationText(
    minutes: Int,
    modifier: Modifier = Modifier,
) {
    val hours = minutes / 60
    val mins = minutes % 60
    val text = when {
        hours > 0 && mins > 0 -> "$hours hr $mins min"
        hours > 0 -> "$hours hr"
        else -> "$mins min"
    }
    Text(text = text, modifier = modifier, style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
