package com.krishifarms.mobile.feature.procurement.presentation.form

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krishifarms.mobile.R
import com.krishifarms.mobile.core.util.AttachmentStorage
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcurementFormScreen(
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: ProcurementFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val attachmentStorage = remember { AttachmentStorage(context) }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    var farmerExpanded by remember { mutableStateOf(false) }
    var cropExpanded by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraTarget by remember { mutableStateOf<AttachmentTarget?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingCameraTarget?.let { target ->
                cameraUri?.let { uri ->
                    val path = attachmentStorage.saveFromUri(uri, target.prefix)
                    when (target) {
                        AttachmentTarget.IMAGE -> viewModel.onImageSelected(path)
                        AttachmentTarget.BILL -> viewModel.onBillSelected(path)
                    }
                }
            }
        }
        pendingCameraTarget = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let {
            pendingCameraTarget?.let { target ->
                val path = attachmentStorage.saveFromUri(it, target.prefix)
                when (target) {
                    AttachmentTarget.IMAGE -> viewModel.onImageSelected(path)
                    AttachmentTarget.BILL -> viewModel.onBillSelected(path)
                }
            }
        }
        pendingCameraTarget = null
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && pendingCameraTarget != null) {
            val file = attachmentStorage.createCameraOutputFile(pendingCameraTarget!!.prefix)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(uiState.saveSuccess, uiState.savedProcurementId) {
        if (uiState.saveSuccess && uiState.savedProcurementId != null) {
            onSaved(uiState.savedProcurementId!!)
        }
    }

    fun launchCamera(target: AttachmentTarget) {
        pendingCameraTarget = target
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    fun launchGallery(target: AttachmentTarget) {
        pendingCameraTarget = target
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.procurement_create_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ExposedDropdownMenuBox(
                    expanded = farmerExpanded,
                    onExpandedChange = { farmerExpanded = !farmerExpanded },
                ) {
                    OutlinedTextField(
                        value = uiState.selectedFarmer?.let { "${it.name} (${it.village})" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.procurement_field_farmer)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = farmerExpanded) },
                        isError = uiState.validationErrors.containsKey("farmer"),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = farmerExpanded,
                        onDismissRequest = { farmerExpanded = false },
                    ) {
                        uiState.farmers.forEach { farmer ->
                            DropdownMenuItem(
                                text = { Text("${farmer.name} (${farmer.village})") },
                                onClick = {
                                    viewModel.onFarmerSelected(farmer.id)
                                    farmerExpanded = false
                                },
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = cropExpanded,
                    onExpandedChange = { cropExpanded = !cropExpanded },
                ) {
                    OutlinedTextField(
                        value = uiState.crop,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.procurement_field_crop)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropExpanded) },
                        isError = uiState.validationErrors.containsKey("crop"),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = cropExpanded,
                        onDismissRequest = { cropExpanded = false },
                    ) {
                        uiState.cropOptions.forEach { crop ->
                            DropdownMenuItem(
                                text = { Text(crop) },
                                onClick = {
                                    viewModel.onCropSelected(crop)
                                    cropExpanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.village,
                    onValueChange = viewModel::onVillageChanged,
                    label = { Text(stringResource(R.string.procurement_field_village)) },
                    isError = uiState.validationErrors.containsKey("village"),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = uiState.bags,
                    onValueChange = viewModel::onBagsChanged,
                    label = { Text(stringResource(R.string.procurement_field_bags)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.validationErrors.containsKey("bags"),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = uiState.weight,
                    onValueChange = viewModel::onWeightChanged,
                    label = { Text(stringResource(R.string.procurement_field_weight)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = uiState.validationErrors.containsKey("weight"),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = uiState.moisture,
                    onValueChange = viewModel::onMoistureChanged,
                    label = { Text(stringResource(R.string.procurement_field_moisture)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = uiState.rate,
                    onValueChange = viewModel::onRateChanged,
                    label = { Text(stringResource(R.string.procurement_field_rate)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = uiState.validationErrors.containsKey("rate"),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = uiState.deductions,
                    onValueChange = viewModel::onDeductionsChanged,
                    label = { Text(stringResource(R.string.procurement_field_deductions)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                NetAmountCard(amount = currencyFormat.format(uiState.netAmount))

                AttachmentPickerRow(
                    title = stringResource(R.string.procurement_field_image),
                    selectedPath = uiState.localImagePath,
                    onCameraClick = { launchCamera(AttachmentTarget.IMAGE) },
                    onGalleryClick = { launchGallery(AttachmentTarget.IMAGE) },
                )
                AttachmentPickerRow(
                    title = stringResource(R.string.procurement_field_bill),
                    selectedPath = uiState.localBillPath,
                    onCameraClick = { launchCamera(AttachmentTarget.BILL) },
                    onGalleryClick = { launchGallery(AttachmentTarget.BILL) },
                    isBill = true,
                )

                uiState.errorMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = viewModel::save,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                    }
                    Text(stringResource(R.string.common_save))
                }
            }
        }
    }
}

@Composable
private fun NetAmountCard(amount: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.procurement_field_net_amount),
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                amount,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                stringResource(R.string.procurement_net_amount_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AttachmentPickerRow(
    title: String,
    selectedPath: String?,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    isBill: Boolean = false,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCameraClick) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.padding(4.dp))
                    Text(stringResource(R.string.procurement_camera))
                }
                OutlinedButton(onClick = onGalleryClick) {
                    Icon(
                        if (isBill) Icons.Default.Receipt else Icons.Default.PhotoLibrary,
                        contentDescription = null,
                    )
                    Spacer(Modifier.padding(4.dp))
                    Text(stringResource(R.string.procurement_gallery))
                }
            }
            selectedPath?.let {
                Text(
                    stringResource(R.string.procurement_attachment_selected),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

private enum class AttachmentTarget(val prefix: String) {
    IMAGE("proc_image"),
    BILL("proc_bill"),
}
