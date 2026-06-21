package com.krishifarms.mobile.feature.document.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.krishifarms.mobile.R
import com.krishifarms.mobile.feature.document.domain.model.Document
import com.krishifarms.mobile.feature.document.domain.model.DocumentType
import com.krishifarms.mobile.feature.document.presentation.permissions.DocumentPermissionRequest
import com.krishifarms.mobile.feature.document.presentation.permissions.rememberDocumentPermissionState

@Composable
fun DocumentPicker(
    documentType: DocumentType,
    documents: List<Document>,
    isSaving: Boolean,
    showSourcePicker: Boolean,
    onAddClick: () -> Unit,
    onDismissSourcePicker: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onGalleryImageSelected: (Uri) -> Unit,
    onDocumentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
) {
    val permissionState = rememberDocumentPermissionState()
    var pendingGalleryPick by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        when (permissionState.pendingRequest) {
            DocumentPermissionRequest.CAMERA -> {
                if (granted) onNavigateToCamera()
            }
            DocumentPermissionRequest.GALLERY -> {
                if (granted) pendingGalleryPick = true
            }
            null -> Unit
        }
        permissionState.clearPending()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let(onGalleryImageSelected)
        pendingGalleryPick = false
    }

    LaunchedEffect(pendingGalleryPick) {
        if (pendingGalleryPick) {
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(documentType.labelRes()),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        documents.forEach { document ->
            DocumentAttachmentRow(
                document = document,
                onClick = { onDocumentClick(document.id) },
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            OutlinedButton(
                onClick = onAddClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Text(stringResource(R.string.document_add_attachment))
            }
        }
    }

    DocumentSourcePickerBottomSheet(
        visible = showSourcePicker,
        onDismiss = onDismissSourcePicker,
        onCameraSelected = {
            if (permissionState.needsCameraPermission()) {
                permissionState.markPending(DocumentPermissionRequest.CAMERA)
                permissionLauncher.launch(permissionState.permissionFor(DocumentPermissionRequest.CAMERA))
            } else {
                onNavigateToCamera()
            }
        },
        onGallerySelected = {
            if (permissionState.needsGalleryPermission()) {
                permissionState.markPending(DocumentPermissionRequest.GALLERY)
                permissionLauncher.launch(permissionState.permissionFor(DocumentPermissionRequest.GALLERY))
            } else {
                pendingGalleryPick = true
            }
        },
    )
}
