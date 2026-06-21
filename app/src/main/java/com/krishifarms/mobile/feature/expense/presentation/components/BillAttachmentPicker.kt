package com.krishifarms.mobile.feature.expense.presentation.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.krishifarms.mobile.R
import com.krishifarms.mobile.core.util.AttachmentStorage
import java.io.File

@Composable
fun BillAttachmentPicker(
    localBillPath: String?,
    onBillSelected: (String) -> Unit,
    onBillCleared: () -> Unit,
    attachmentStorage: AttachmentStorage,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var cameraFile by remember { mutableStateOf<File?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            val path = attachmentStorage.saveFromUri(it, "expense_bill")
            onBillSelected(path)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            cameraFile?.absolutePath?.let(onBillSelected)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val file = attachmentStorage.createCameraOutputFile("expense_bill")
            cameraFile = file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            cameraLauncher.launch(uri)
        }
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.expense_field_bill),
                style = MaterialTheme.typography.titleSmall,
            )

            val previewModel = when {
                !localBillPath.isNullOrBlank() && File(localBillPath).exists() -> localBillPath
                else -> null
            }

            if (previewModel != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(previewModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.expense_field_bill),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop,
                )
                OutlinedButton(onClick = onBillCleared, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.expense_remove_bill))
                }
            } else {
                Text(
                    text = stringResource(R.string.expense_bill_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Text(
                        text = stringResource(R.string.expense_pick_gallery),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Button(
                    onClick = {
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA,
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                val file = attachmentStorage.createCameraOutputFile("expense_bill")
                                cameraFile = file
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file,
                                )
                                cameraLauncher.launch(uri)
                            }
                            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Text(
                        text = stringResource(R.string.expense_take_photo),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}
