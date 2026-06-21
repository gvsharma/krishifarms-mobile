package com.krishifarms.mobile.core.security.rbac

import androidx.compose.runtime.Composable
import com.krishifarms.mobile.core.navigation.Routes
import com.krishifarms.mobile.core.security.session.UserContext
import com.krishifarms.mobile.core.ui.rbac.AccessDeniedScreen
import com.krishifarms.mobile.feature.dashboard.presentation.DashboardCardType

object FeatureVisibility {
    fun showModule(context: UserContext?, moduleId: String): Boolean {
        if (context == null) return false
        return context.accessibleModules.contains(moduleId) ||
            MenuRegistry.entries.any { it.moduleId == moduleId && context.permissions.contains(it.viewPermission) }
    }
}

@Composable
fun GuardedRoute(
    route: String,
    permissionManager: PermissionManager,
    onNavigateToDashboard: () -> Unit,
    content: @Composable () -> Unit,
) {
    val guard = NavigationGuard(permissionManager)
    if (guard.canAccess(route)) {
        content()
    } else {
        AccessDeniedScreen(onNavigateBack = onNavigateToDashboard)
    }
}

object DashboardCardAccess {
    fun visibleCards(context: UserContext?): Set<DashboardCardType> {
        if (context == null) return emptySet()
        return DashboardCardType.entries.filter { card ->
            when (card) {
                DashboardCardType.TODAYS_PROCUREMENT -> FeatureVisibility.showModule(context, "procurement")
                DashboardCardType.TODAYS_EXPENSES -> FeatureVisibility.showModule(context, "expenses")
                DashboardCardType.TODAYS_COLLECTIONS -> FeatureVisibility.showModule(context, "collections")
                DashboardCardType.PENDING_FARMER_PAYMENTS -> FeatureVisibility.showModule(context, "farmer_payments")
                DashboardCardType.PENDING_COLLECTIONS -> FeatureVisibility.showModule(context, "collections")
                DashboardCardType.WORKER_ATTENDANCE -> FeatureVisibility.showModule(context, "attendance")
                DashboardCardType.ACTIVE_RENTALS -> FeatureVisibility.showModule(context, "rentals")
            }
        }.toSet()
    }
}
