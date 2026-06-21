package com.krishifarms.mobile.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Canopia-inspired elevated card — 16dp radius, 1dp elevation, surface variant tint.
 */
@Composable
fun KfCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
    )
    val elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    val shape = MaterialTheme.shapes.medium

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            colors = colors,
            elevation = elevation,
            shape = shape,
            content = content,
        )
    } else {
        Card(
            modifier = modifier,
            colors = colors,
            elevation = elevation,
            shape = shape,
            content = content,
        )
    }
}
