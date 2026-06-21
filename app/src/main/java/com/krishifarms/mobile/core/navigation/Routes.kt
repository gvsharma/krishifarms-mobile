package com.krishifarms.mobile.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Folder
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
import androidx.compose.material.icons.filled.Verified
import androidx.compose.ui.graphics.vector.ImageVector
import com.krishifarms.mobile.R

object Routes {
    const val AUTH_GRAPH = "auth_graph"
    const val LOGIN = "login"
    const val SESSION_LOADING = "session_loading"

    const val MAIN_GRAPH = "main_graph"
    const val DASHBOARD = "dashboard"
    const val FARMERS = "farmers"
    const val FARMER_DETAIL = "farmers/{farmerId}"
    const val FARMER_ADD = "farmers/add"
    const val FARMER_EDIT = "farmers/{farmerId}/edit"
    const val FARMER_ID_ARG = "farmerId"

    fun farmerDetail(farmerId: String) = "farmers/$farmerId"
    fun farmerEdit(farmerId: String) = "farmers/$farmerId/edit"

    const val FARMS = "farms"
    const val PROCUREMENT = "procurement"
    const val PROCUREMENT_LIST = "procurement/list"
    const val FARMER_PAYMENTS = "farmer_payments"
    const val WORKERS = "workers"
    const val WORKER_DETAIL = "workers/{workerId}"
    const val WORKER_FORM = "workers/form"
    const val WORKER_FORM_EDIT = "workers/form/{workerId}"
    const val WORK_ORDERS = "work_orders"
    const val WORK_ORDER_FORM = "work_orders/form"
    const val WORK_ORDER_DETAIL = "work_orders/{workOrderId}"
    const val ATTENDANCE = "attendance"

    const val WORKER_ID_ARG = "workerId"
    const val WORK_ORDER_ID_ARG = "workOrderId"

    fun workerDetail(workerId: String) = "workers/$workerId"
    fun workerFormEdit(workerId: String) = "workers/form/$workerId"
    fun workOrderDetail(workOrderId: String) = "work_orders/$workOrderId"
    const val EXPENSES = "expenses"
    const val COLLECTIONS = "collections"
    const val PAYMENTS = "payments"
    const val VEHICLES = "vehicles"
    const val VEHICLE_TRIPS = "vehicle_trips"
    const val ASSETS = "assets"
    const val RENTALS = "rentals"
    const val DOCUMENTS = "documents"
    const val SETTINGS = "settings"
    const val SYNC = "sync"
}

data class FeatureDestination(
    val route: String,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val icon: ImageVector,
)

val MainFeatureDestinations: List<FeatureDestination> = listOf(
    FeatureDestination(Routes.DASHBOARD, R.string.nav_dashboard, R.string.dashboard_welcome, Icons.Default.Dashboard),
    FeatureDestination(Routes.FARMERS, R.string.nav_farmers, R.string.farmers_subtitle, Icons.Default.People),
    FeatureDestination(Routes.FARMS, R.string.nav_farms, R.string.farms_subtitle, Icons.Default.Agriculture),
    FeatureDestination(Routes.PROCUREMENT, R.string.nav_procurement, R.string.procurement_subtitle, Icons.Default.ShoppingCart),
    FeatureDestination(Routes.FARMER_PAYMENTS, R.string.nav_farmer_payments, R.string.farmer_payments_subtitle, Icons.Default.Payments),
    FeatureDestination(Routes.WORKERS, R.string.nav_workers, R.string.workers_subtitle, Icons.Default.Groups),
    FeatureDestination(Routes.WORK_ORDERS, R.string.nav_work_orders, R.string.work_orders_subtitle, Icons.AutoMirrored.Filled.Assignment),
    FeatureDestination(Routes.ATTENDANCE, R.string.nav_attendance, R.string.attendance_subtitle, Icons.Default.Schedule),
    FeatureDestination(Routes.EXPENSES, R.string.nav_expenses, R.string.expenses_subtitle, Icons.AutoMirrored.Filled.ReceiptLong),
    FeatureDestination(Routes.COLLECTIONS, R.string.nav_collections, R.string.collections_subtitle, Icons.Default.AttachMoney),
    FeatureDestination(Routes.PAYMENTS, R.string.nav_payments, R.string.payments_subtitle, Icons.Default.Payments),
    FeatureDestination(Routes.VEHICLES, R.string.nav_vehicles, R.string.vehicles_subtitle, Icons.Default.DirectionsCar),
    FeatureDestination(Routes.VEHICLE_TRIPS, R.string.nav_vehicle_trips, R.string.vehicle_trips_subtitle, Icons.Default.LocalShipping),
    FeatureDestination(Routes.ASSETS, R.string.nav_assets, R.string.assets_subtitle, Icons.Default.Inventory2),
    FeatureDestination(Routes.RENTALS, R.string.nav_rentals, R.string.rentals_subtitle, Icons.Default.Grass),
    FeatureDestination(Routes.DOCUMENTS, R.string.nav_documents, R.string.documents_subtitle, Icons.Default.Description),
    FeatureDestination(Routes.SETTINGS, R.string.nav_settings, R.string.settings_subtitle, Icons.Default.Settings),
)

val SyncDestination = FeatureDestination(
    route = Routes.SYNC,
    titleRes = R.string.nav_sync,
    subtitleRes = R.string.sync_subtitle,
    icon = Icons.Default.Sync,
)
