package com.krishifarms.mobile.feature.worker.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.krishifarms.mobile.core.navigation.Routes
import com.krishifarms.mobile.core.security.rbac.GuardedRoute
import com.krishifarms.mobile.core.security.rbac.PermissionManager
import com.krishifarms.mobile.feature.worker.presentation.attendance.AttendanceScreen
import com.krishifarms.mobile.feature.worker.presentation.detail.WorkerDetailScreen
import com.krishifarms.mobile.feature.worker.presentation.form.WorkerFormScreen
import com.krishifarms.mobile.feature.worker.presentation.list.WorkerListScreen
import com.krishifarms.mobile.feature.worker.presentation.workorder.WorkOrderDetailScreen
import com.krishifarms.mobile.feature.worker.presentation.workorder.WorkOrderFormScreen
import com.krishifarms.mobile.feature.worker.presentation.workorder.WorkOrderListScreen

fun NavGraphBuilder.workerRoutes(
    navController: NavHostController,
    permissionManager: PermissionManager,
    onNavigateToDashboard: () -> Unit,
    guardedNavigate: (String) -> Unit,
) {
    composable(Routes.WORKERS) {
        GuardedRoute(Routes.WORKERS, permissionManager, onNavigateToDashboard) {
            WorkerListScreen(
                onWorkerClick = { workerId -> guardedNavigate(Routes.workerDetail(workerId)) },
                onAddWorker = { guardedNavigate(Routes.WORKER_FORM) },
                onNavigateToAttendance = { guardedNavigate(Routes.ATTENDANCE) },
                onNavigateToWorkOrders = { guardedNavigate(Routes.WORK_ORDERS) },
            )
        }
    }

    composable(
        route = Routes.WORKER_DETAIL,
        arguments = listOf(navArgument(Routes.WORKER_ID_ARG) { type = NavType.StringType }),
    ) {
        GuardedRoute(Routes.WORKER_DETAIL, permissionManager, onNavigateToDashboard) {
            WorkerDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { workerId -> guardedNavigate(Routes.workerFormEdit(workerId)) },
                onWorkOrderClick = { workOrderId -> guardedNavigate(Routes.workOrderDetail(workOrderId)) },
            )
        }
    }

    composable(Routes.WORKER_FORM) {
        GuardedRoute(Routes.WORKER_FORM, permissionManager, onNavigateToDashboard) {
            WorkerFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
    }

    composable(
        route = Routes.WORKER_FORM_EDIT,
        arguments = listOf(navArgument(Routes.WORKER_ID_ARG) { type = NavType.StringType }),
    ) {
        GuardedRoute(Routes.WORKER_FORM_EDIT, permissionManager, onNavigateToDashboard) {
            WorkerFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
    }

    composable(Routes.ATTENDANCE) {
        GuardedRoute(Routes.ATTENDANCE, permissionManager, onNavigateToDashboard) {
            AttendanceScreen(onBack = { navController.popBackStack() })
        }
    }

    composable(Routes.WORK_ORDERS) {
        GuardedRoute(Routes.WORK_ORDERS, permissionManager, onNavigateToDashboard) {
            WorkOrderListScreen(
                onBack = { navController.popBackStack() },
                onWorkOrderClick = { id -> guardedNavigate(Routes.workOrderDetail(id)) },
                onAddWorkOrder = { guardedNavigate(Routes.WORK_ORDER_FORM) },
            )
        }
    }

    composable(Routes.WORK_ORDER_FORM) {
        GuardedRoute(Routes.WORK_ORDER_FORM, permissionManager, onNavigateToDashboard) {
            WorkOrderFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
    }

    composable(
        route = Routes.WORK_ORDER_DETAIL,
        arguments = listOf(navArgument(Routes.WORK_ORDER_ID_ARG) { type = NavType.StringType }),
    ) {
        GuardedRoute(Routes.WORK_ORDER_DETAIL, permissionManager, onNavigateToDashboard) {
            WorkOrderDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
