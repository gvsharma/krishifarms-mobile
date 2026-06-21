package com.krishifarms.mobile.feature.expense.presentation.detail

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
import com.krishifarms.mobile.feature.expense.domain.model.Expense
import com.krishifarms.mobile.feature.expense.presentation.components.labelRes
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    onBack: () -> Unit,
    viewModel: ExpenseDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.expense_detail_title)) },
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
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.expense == null -> {
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
                val expense = checkNotNull(uiState.expense)
                ExpenseDetailContent(
                    expense = expense,
                    currencyFormat = currencyFormat,
                    dateFormat = dateFormat,
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
private fun ExpenseDetailContent(
    expense: Expense,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.expense_field_category),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(expense.category.labelRes()),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(8.dp))
                androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                    SyncStatusIcon(status = expense.syncStatus)
                    Spacer(Modifier.padding(4.dp))
                    Text(syncStatusLabel(expense.syncStatus), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        DetailField(
            label = stringResource(R.string.expense_field_amount),
            value = currencyFormat.format(expense.amount),
            emphasized = true,
        )
        DetailField(
            label = stringResource(R.string.expense_field_date),
            value = dateFormat.format(Date(expense.expenseDate)),
        )
        DetailField(
            label = stringResource(R.string.expense_field_description),
            value = expense.description,
        )
        expense.vendor?.let {
            DetailField(label = stringResource(R.string.expense_field_vendor), value = it)
        }
        expense.paymentMethod?.let {
            DetailField(
                label = stringResource(R.string.expense_field_payment_method),
                value = stringResource(it.labelRes()),
            )
        }

        BillSection(
            localPath = expense.localBillPath,
            remoteUrl = expense.remoteBillUrl,
        )
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
private fun BillSection(
    localPath: String?,
    remoteUrl: String?,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.expense_field_bill),
                style = MaterialTheme.typography.titleSmall,
            )
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
                    contentDescription = stringResource(R.string.expense_field_bill),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    stringResource(R.string.expense_no_bill),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
