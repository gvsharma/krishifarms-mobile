package com.krishifarms.mobile.core.security.rbac

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.graphics.vector.ImageVector
import com.krishifarms.mobile.R
import com.krishifarms.mobile.core.navigation.Routes
import com.krishifarms.mobile.core.security.session.UserContext

data class MenuEntry(
    val moduleId: String,
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val order: Int,
    val viewPermission: Permission,
)

object MenuRegistry {
    val entries: List<MenuEntry> = listOf(
        MenuEntry("dashboard", Routes.DASHBOARD, R.string.nav_dashboard, Icons.Default.Dashboard, 0, Permission.REPORT_VIEW),
        MenuEntry("farmers", Routes.FARMERS, R.string.nav_farmers, Icons.Default.People, 10, Permission.FARMER_VIEW),
        MenuEntry("farms", Routes.FARMS, R.string.nav_farms, Icons.Default.Agriculture, 20, Permission.FARM_VIEW),
        MenuEntry("procurement", Routes.PROCUREMENT, R.string.nav_procurement, Icons.Default.ShoppingCart, 30, Permission.PROCUREMENT_VIEW),
        MenuEntry("farmer_payments", Routes.FARMER_PAYMENTS, R.string.nav_farmer_payments, Icons.Default.Payments, 40, Permission.PAYMENT_VIEW),
        MenuEntry("workers", Routes.WORKERS, R.string.nav_workers, Icons.Default.Groups, 50, Permission.WORKER_VIEW),
        MenuEntry("work_orders", Routes.WORK_ORDERS, R.string.nav_work_orders, Icons.AutoMirrored.Filled.Assignment, 60, Permission.WORK_ORDER_VIEW),
        MenuEntry("attendance", Routes.ATTENDANCE, R.string.nav_attendance, Icons.Default.Schedule, 70, Permission.ATTENDANCE_VIEW),
        MenuEntry("expenses", Routes.EXPENSES, R.string.nav_expenses, Icons.AutoMirrored.Filled.ReceiptLong, 80, Permission.EXPENSE_VIEW),
        MenuEntry("collections", Routes.COLLECTIONS, R.string.nav_collections, Icons.Default.AttachMoney, 90, Permission.COLLECTION_VIEW),
        MenuEntry("payments", Routes.PAYMENTS, R.string.nav_payments, Icons.Default.Payments, 100, Permission.PAYMENT_VIEW),
        MenuEntry("vehicles", Routes.VEHICLES, R.string.nav_vehicles, Icons.Default.DirectionsCar, 110, Permission.VEHICLE_VIEW),
        MenuEntry("vehicle_trips", Routes.VEHICLE_TRIPS, R.string.nav_vehicle_trips, Icons.Default.LocalShipping, 120, Permission.TRIP_VIEW),
        MenuEntry("assets", Routes.ASSETS, R.string.nav_assets, Icons.Default.Inventory2, 130, Permission.ASSET_VIEW),
        MenuEntry("rentals", Routes.RENTALS, R.string.nav_rentals, Icons.Default.Grass, 140, Permission.RENTAL_VIEW),
        MenuEntry("documents", Routes.DOCUMENTS, R.string.nav_documents, Icons.Default.Description, 150, Permission.DOCUMENT_VIEW),
        MenuEntry("settings", Routes.SETTINGS, R.string.nav_settings, Icons.Default.Settings, 160, Permission.SETTINGS_VIEW),
        MenuEntry("sync", Routes.SYNC, R.string.nav_sync, Icons.Default.Sync, 170, Permission.SYNC_MANAGE),
    )

    fun allModuleIds(): Set<String> = entries.map { it.moduleId }.toSet()

    fun viewPermissionsForModule(moduleId: String): List<String> =
        entries.firstOrNull { it.moduleId == moduleId }?.let { listOf(it.viewPermission.code) } ?: emptyList()

    fun entryForRoute(route: String): MenuEntry? =
        entries.firstOrNull { route == it.route || route.startsWith("${it.route}/") }
}

class DynamicMenuProvider {
    fun visibleEntries(context: UserContext): List<MenuEntry> {
        val modules = context.accessibleModules
        return MenuRegistry.entries
            .filter { entry ->
                entry.moduleId == "dashboard" ||
                    modules.contains(entry.moduleId) ||
                    context.permissions.contains(entry.viewPermission)
            }
            .sortedBy { it.order }
    }
}
