package com.krishifarms.mobile.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.krishifarms.mobile.R
import com.krishifarms.mobile.core.common.SyncStatus

@Composable
fun SyncStatusIcon(
    status: SyncStatus,
    modifier: Modifier = Modifier,
) {
    val (icon, tint) = when (status) {
        SyncStatus.SYNCED -> Icons.Default.CloudDone to MaterialTheme.colorScheme.primary
        SyncStatus.PENDING_CREATE,
        SyncStatus.PENDING_UPDATE,
        SyncStatus.PENDING_DELETE,
        -> Icons.Default.CloudUpload to MaterialTheme.colorScheme.tertiary
        SyncStatus.SYNC_FAILED -> Icons.Default.ErrorOutline to MaterialTheme.colorScheme.error
    }
    Icon(
        imageVector = icon,
        contentDescription = syncStatusLabel(status),
        tint = tint,
        modifier = modifier.size(20.dp),
    )
}

@Composable
fun syncStatusLabel(status: SyncStatus): String = when (status) {
    SyncStatus.SYNCED -> stringResource(R.string.common_sync_status_synced)
    SyncStatus.SYNC_FAILED -> stringResource(R.string.common_sync_status_failed)
    else -> stringResource(R.string.common_sync_status_pending)
}
