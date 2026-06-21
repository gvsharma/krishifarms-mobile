package com.krishifarms.mobile.feature.document.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.krishifarms.mobile.R
import com.krishifarms.mobile.core.ui.components.SyncStatusIcon
import com.krishifarms.mobile.feature.document.domain.model.Document
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentSourcePickerBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.document_source_picker_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.document_source_camera)) },
                leadingContent = {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    onDismiss()
                    onCameraSelected()
                },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.document_source_gallery)) },
                leadingContent = {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    onDismiss()
                    onGallerySelected()
                },
            )
        }
    }
}

@Composable
fun DocumentAttachmentRow(
    document: Document,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageModel = document.localPath.takeIf { File(it).exists() } ?: document.remoteUrl
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = imageModel,
            contentDescription = stringResource(document.type.labelRes()),
            modifier = Modifier.size(56.dp),
            contentScale = ContentScale.Crop,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(document.type.labelRes()),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = document.fileName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        SyncStatusIcon(status = document.syncStatus)
    }
}
