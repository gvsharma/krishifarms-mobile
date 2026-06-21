package com.krishifarms.mobile.feature.dashboard.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krishifarms.mobile.R
import com.krishifarms.mobile.feature.dashboard.presentation.components.DashboardStatCard
import java.text.DateFormat
import java.util.Date

@StringRes
val DashboardCardType.labelRes: Int
    get() = when (this) {
        DashboardCardType.TODAYS_PROCUREMENT -> R.string.dashboard_card_todays_procurement
        DashboardCardType.TODAYS_EXPENSES -> R.string.dashboard_card_todays_expenses
        DashboardCardType.TODAYS_COLLECTIONS -> R.string.dashboard_card_todays_collections
        DashboardCardType.PENDING_FARMER_PAYMENTS -> R.string.dashboard_card_pending_farmer_payments
        DashboardCardType.PENDING_COLLECTIONS -> R.string.dashboard_card_pending_collections
        DashboardCardType.WORKER_ATTENDANCE -> R.string.dashboard_card_worker_attendance
        DashboardCardType.ACTIVE_RENTALS -> R.string.dashboard_card_active_rentals
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onCardClick: (DashboardCardType) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        DashboardUiState.Loading -> DashboardLoadingState(modifier = modifier)

        DashboardUiState.Empty -> DashboardEmptyState(
            modifier = modifier,
            onRefresh = viewModel::refresh,
        )

        is DashboardUiState.Error -> DashboardErrorState(
            modifier = modifier,
            message = state.message,
            onRetry = viewModel::retry,
        )

        is DashboardUiState.Success -> PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = modifier.fillMaxSize(),
        ) {
            DashboardContent(
                state = state,
                onCardClick = onCardClick,
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Success,
    onCardClick: (DashboardCardType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val subtitle = buildString {
            append(
                stringResource(
                    R.string.dashboard_last_updated,
                    DateFormat.getDateTimeInstance().format(Date(state.lastUpdatedAt)),
                ),
            )
            if (state.isFromCache) {
                append(" • ")
                append(stringResource(R.string.dashboard_cached_data))
            }
        }

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                items = state.cards,
                key = { it.type.name },
            ) { card ->
                DashboardStatCard(
                    type = card.type,
                    metric = card.metric,
                    icon = card.type.icon(),
                    onClick = { onCardClick(card.type) },
                )
            }
        }
    }
}

@Composable
private fun DashboardLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.common_loading),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun DashboardEmptyState(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.dashboard_empty_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.dashboard_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onRefresh) {
                Text(stringResource(R.string.common_retry))
            }
        }
    }
}

@Composable
private fun DashboardErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.common_error),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onRetry) {
                Text(stringResource(R.string.common_retry))
            }
        }
    }
}

@Composable
private fun DashboardCardType.icon(): ImageVector {
    return when (this) {
        DashboardCardType.TODAYS_PROCUREMENT -> Icons.Outlined.ShoppingCart
        DashboardCardType.TODAYS_EXPENSES -> Icons.Outlined.AttachMoney
        DashboardCardType.TODAYS_COLLECTIONS -> Icons.Outlined.AccountBalanceWallet
        DashboardCardType.PENDING_FARMER_PAYMENTS -> Icons.Outlined.Payments
        DashboardCardType.PENDING_COLLECTIONS -> Icons.Outlined.Assignment
        DashboardCardType.WORKER_ATTENDANCE -> Icons.Outlined.Groups
        DashboardCardType.ACTIVE_RENTALS -> Icons.Outlined.HomeWork
    }
}
