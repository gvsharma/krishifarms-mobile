package com.krishifarms.mobile.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.krishifarms.mobile.R
import com.krishifarms.mobile.core.security.rbac.NavigationGuard
import com.krishifarms.mobile.core.security.session.UserContext

enum class BottomNavTab(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val moduleIds: Set<String>,
) {
    HOME(Routes.DASHBOARD, R.string.bottom_nav_home, Icons.Default.Dashboard, setOf("dashboard")),
    OPERATIONS(Routes.FARMERS, R.string.bottom_nav_operations, Icons.Default.Work, setOf("farmers", "farms", "procurement", "workers", "work_orders", "attendance")),
    COLLECT(Routes.COLLECTIONS, R.string.bottom_nav_collect, Icons.Default.AttachMoney, setOf("collections", "farmer_payments")),
    FINANCE(Routes.EXPENSES, R.string.bottom_nav_finance, Icons.Default.AttachMoney, setOf("expenses", "payments")),
    MORE(Routes.SETTINGS, R.string.bottom_nav_more, Icons.Default.MoreHoriz, setOf("documents", "settings", "sync", "vehicles", "assets", "rentals")),
}

@Composable
fun MainBottomNav(
    navController: NavController,
    userContext: UserContext?,
    navigationGuard: NavigationGuard,
    onForbidden: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val modules = userContext?.accessibleModules ?: emptySet()

    val visibleTabs = BottomNavTab.entries.filter { tab ->
        tab == BottomNavTab.HOME ||
            tab.moduleIds.any { modules.contains(it) }
    }

    if (visibleTabs.size <= 1) return

    NavigationBar(modifier = modifier) {
        visibleTabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route || tab.moduleIds.any { module ->
                    currentRoute?.contains(module.replace('_', '/')) == true
                },
                onClick = {
                    val targetRoute = when (tab) {
                        BottomNavTab.OPERATIONS -> firstAccessibleRoute(
                            modules,
                            listOf(Routes.FARMERS, Routes.PROCUREMENT, Routes.WORKERS),
                        )
                        BottomNavTab.COLLECT -> firstAccessibleRoute(
                            modules,
                            listOf(Routes.COLLECTIONS, Routes.FARMER_PAYMENTS),
                        )
                        BottomNavTab.FINANCE -> firstAccessibleRoute(
                            modules,
                            listOf(Routes.EXPENSES, Routes.PAYMENTS),
                        )
                        BottomNavTab.MORE -> Routes.SETTINGS
                        else -> tab.route
                    }
                    navController.guardedNavigate(targetRoute, navigationGuard, onForbidden) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.labelRes)) },
            )
        }
    }
}

private fun firstAccessibleRoute(modules: Set<String>, candidates: List<String>): String {
    val moduleToRoute = mapOf(
        "farmers" to Routes.FARMERS,
        "procurement" to Routes.PROCUREMENT,
        "workers" to Routes.WORKERS,
        "collections" to Routes.COLLECTIONS,
        "farmer_payments" to Routes.FARMER_PAYMENTS,
        "expenses" to Routes.EXPENSES,
        "payments" to Routes.PAYMENTS,
    )
    return candidates.firstOrNull { route ->
        moduleToRoute.entries.any { (module, mappedRoute) -> mappedRoute == route && modules.contains(module) }
    } ?: candidates.first()
}
