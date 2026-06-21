package com.krishifarms.mobile.core.navigation

import com.krishifarms.mobile.feature.dashboard.presentation.DashboardCardType

fun DashboardCardType.toRoute(): String {
    return when (this) {
        DashboardCardType.TODAYS_PROCUREMENT -> ProcurementRoutes.LIST
        DashboardCardType.TODAYS_EXPENSES -> ExpenseRoutes.LIST
        DashboardCardType.TODAYS_COLLECTIONS -> Routes.COLLECTIONS
        DashboardCardType.PENDING_FARMER_PAYMENTS -> Routes.FARMER_PAYMENTS
        DashboardCardType.PENDING_COLLECTIONS -> Routes.COLLECTIONS
        DashboardCardType.WORKER_ATTENDANCE -> Routes.ATTENDANCE
        DashboardCardType.ACTIVE_RENTALS -> Routes.RENTALS
    }
}
