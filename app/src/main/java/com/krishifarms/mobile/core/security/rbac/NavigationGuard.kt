package com.krishifarms.mobile.core.security.rbac

import com.krishifarms.mobile.core.navigation.ExpenseRoutes
import com.krishifarms.mobile.core.navigation.ProcurementRoutes
import com.krishifarms.mobile.core.navigation.Routes
import com.krishifarms.mobile.feature.document.navigation.DocumentRoutes

object ScreenAccess {
    private val routePermissions: List<Pair<String, Set<Permission>>> = listOf(
        Routes.DASHBOARD to emptySet(),
        Routes.FARMERS to setOf(Permission.FARMER_VIEW),
        Routes.FARMER_ADD to setOf(Permission.FARMER_CREATE),
        Routes.FARMER_DETAIL to setOf(Permission.FARMER_VIEW),
        Routes.FARMER_EDIT to setOf(Permission.FARMER_UPDATE),
        Routes.FARMS to setOf(Permission.FARM_VIEW),
        Routes.PROCUREMENT to setOf(Permission.PROCUREMENT_VIEW),
        ProcurementRoutes.LIST to setOf(Permission.PROCUREMENT_VIEW),
        ProcurementRoutes.CREATE to setOf(Permission.PROCUREMENT_CREATE),
        ProcurementRoutes.DETAIL to setOf(Permission.PROCUREMENT_VIEW),
        Routes.FARMER_PAYMENTS to setOf(Permission.PAYMENT_VIEW),
        Routes.WORKERS to setOf(Permission.WORKER_VIEW),
        Routes.WORKER_DETAIL to setOf(Permission.WORKER_VIEW),
        Routes.WORKER_FORM to setOf(Permission.WORKER_CREATE),
        Routes.WORKER_FORM_EDIT to setOf(Permission.WORKER_UPDATE),
        Routes.WORK_ORDERS to setOf(Permission.WORK_ORDER_VIEW),
        Routes.WORK_ORDER_FORM to setOf(Permission.WORK_ORDER_CREATE),
        Routes.WORK_ORDER_DETAIL to setOf(Permission.WORK_ORDER_VIEW),
        Routes.ATTENDANCE to setOf(Permission.ATTENDANCE_VIEW),
        Routes.EXPENSES to setOf(Permission.EXPENSE_VIEW),
        ExpenseRoutes.LIST to setOf(Permission.EXPENSE_VIEW),
        ExpenseRoutes.FORM to setOf(Permission.EXPENSE_CREATE),
        ExpenseRoutes.DETAIL to setOf(Permission.EXPENSE_VIEW),
        Routes.COLLECTIONS to setOf(Permission.COLLECTION_VIEW),
        Routes.PAYMENTS to setOf(Permission.PAYMENT_VIEW),
        Routes.VEHICLES to setOf(Permission.VEHICLE_VIEW),
        Routes.VEHICLE_TRIPS to setOf(Permission.TRIP_VIEW),
        Routes.ASSETS to setOf(Permission.ASSET_VIEW),
        Routes.RENTALS to setOf(Permission.RENTAL_VIEW),
        Routes.DOCUMENTS to setOf(Permission.DOCUMENT_VIEW),
        DocumentRoutes.LIST to setOf(Permission.DOCUMENT_VIEW),
        DocumentRoutes.UPLOAD to setOf(Permission.DOCUMENT_CREATE),
        DocumentRoutes.CAPTURE to setOf(Permission.DOCUMENT_CREATE),
        DocumentRoutes.PREVIEW to setOf(Permission.DOCUMENT_VIEW),
        Routes.SETTINGS to setOf(Permission.SETTINGS_VIEW),
        Routes.SYNC to setOf(Permission.SYNC_MANAGE, Permission.SETTINGS_VIEW),
    )

    fun requiredPermissions(route: String): Set<Permission> {
        val normalized = route.substringBefore("?")
        routePermissions.firstOrNull { (pattern, _) ->
            matches(normalized, pattern)
        }?.second?.let { return it }

        MenuRegistry.entryForRoute(normalized)?.let { return setOf(it.viewPermission) }
        return emptySet()
    }

    private fun matches(route: String, pattern: String): Boolean {
        if (pattern.contains("{")) {
            val prefix = pattern.substringBefore("{")
            return route.startsWith(prefix)
        }
        return route == pattern || route.startsWith("$pattern/")
    }
}

class NavigationGuard(
    private val permissionManager: PermissionManager,
) {
    fun canAccess(route: String): Boolean {
        val required = ScreenAccess.requiredPermissions(route)
        if (required.isEmpty()) return permissionManager.isAuthenticated()
        return permissionManager.hasAny(*required.toTypedArray())
    }

    fun requiredPermissions(route: String): Set<Permission> = ScreenAccess.requiredPermissions(route)

    fun fallbackRoute(): String = Routes.DASHBOARD
}
