package com.krishifarms.mobile.core.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.krishifarms.mobile.core.security.rbac.NavigationGuard

fun NavController.guardedNavigate(
    route: String,
    navigationGuard: NavigationGuard,
    onForbidden: () -> Unit,
    builder: (androidx.navigation.NavOptionsBuilder.() -> Unit)? = null,
) {
    if (!navigationGuard.canAccess(route)) {
        onForbidden()
        navigate(navigationGuard.fallbackRoute()) {
            popUpTo(graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        return
    }
    if (builder != null) {
        navigate(route, builder)
    } else {
        navigate(route)
    }
}
