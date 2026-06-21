package com.krishifarms.mobile.feature.farmer.presentation.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krishifarms.mobile.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FarmerFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: FarmerFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEdit = uiState.farmerId != null

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (isEdit) R.string.farmer_edit_title else R.string.farmer_add_title,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.farmer_field_name)) },
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(fieldErrorText(it)) } },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.village,
                        onValueChange = viewModel::onVillageChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.farmer_field_village)) },
                        isError = uiState.villageError != null,
                        supportingText = uiState.villageError?.let { { Text(fieldErrorText(it)) } },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.phone,
                        onValueChange = viewModel::onPhoneChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.farmer_field_phone)) },
                        isError = uiState.phoneError != null,
                        supportingText = uiState.phoneError?.let { { Text(fieldErrorText(it)) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.bankDetails,
                        onValueChange = viewModel::onBankDetailsChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.farmer_field_bank_details)) },
                        isError = uiState.bankDetailsError != null,
                        supportingText = uiState.bankDetailsError?.let { { Text(fieldErrorText(it)) } },
                        minLines = 2,
                    )
                    OutlinedTextField(
                        value = uiState.landAcres,
                        onValueChange = viewModel::onLandAcresChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.farmer_field_land_acres)) },
                        isError = uiState.landAcresError != null,
                        supportingText = uiState.landAcresError?.let { { Text(fieldErrorText(it)) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.farmer_field_crop_types),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            uiState.availableCropTypes.forEach { crop ->
                                FilterChip(
                                    selected = uiState.selectedCropTypes.contains(crop),
                                    onClick = { viewModel.onCropTypeToggle(crop) },
                                    label = { Text(crop) },
                                )
                            }
                        }
                        if (uiState.cropTypesError != null) {
                            Text(
                                text = fieldErrorText(uiState.cropTypesError!!),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = viewModel::save,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving,
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(stringResource(R.string.common_save))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun fieldErrorText(error: FarmerFormFieldError): String = when (error) {
    FarmerFormFieldError.NAME_REQUIRED -> stringResource(R.string.farmer_error_name_required)
    FarmerFormFieldError.VILLAGE_REQUIRED -> stringResource(R.string.farmer_error_village_required)
    FarmerFormFieldError.PHONE_REQUIRED -> stringResource(R.string.farmer_error_phone_required)
    FarmerFormFieldError.PHONE_INVALID -> stringResource(R.string.error_mobile_invalid)
    FarmerFormFieldError.BANK_DETAILS_REQUIRED -> stringResource(R.string.farmer_error_bank_required)
    FarmerFormFieldError.LAND_ACRES_INVALID -> stringResource(R.string.farmer_error_land_acres_invalid)
    FarmerFormFieldError.CROP_TYPES_REQUIRED -> stringResource(R.string.farmer_error_crop_types_required)
}
