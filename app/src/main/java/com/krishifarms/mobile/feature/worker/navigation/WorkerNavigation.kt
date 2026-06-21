package com.krishifarms.mobile.feature.worker.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.krishifarms.mobile.core.navigation.Routes
import com.krishifarms.mobile.feature.worker.presentation.attendance.AttendanceScreen
import com.krishifarms.mobile.feature.worker.presentation.detail.WorkerDetailScreen
import com.krishifarms.mobile.feature.worker.presentation.form.WorkerFormScreen
import com.krishifarms.mobile.feature.worker.presentation.list.WorkerListScreen
import com.krishifarms.mobile.feature.worker.presentation.workorder.WorkOrderDetailScreen
import com.krishifarms.mobile.feature.worker.presentation.workorder.WorkOrderFormScreen
import com.krishifarms.mobile.feature.worker.presentation.workorder.WorkOrderListScreen

fun NavGraphBuilder.workerRoutes(navController: NavHostController) {
    composable(Routes.WORKERS) {
        WorkerListScreen(
            onWorkerClick = { workerId ->
                navController.navigate(Routes.workerDetail(workerId))
            },
            onAddWorker = { navController.navigate(Routes.WORKER_FORM) },
            onNavigateToAttendance = { navController.navigate(Routes.ATTENDANCE) },
            onNavigateToWorkOrders = { navController.navigate(Routes.WORK_ORDERS) },
        )
    }

    composable(
        route = Routes.WORKER_DETAIL,
        arguments = listOf(navArgument(Routes.WORKER_ID_ARG) { type = NavType.StringType }),
    ) {
        WorkerDetailScreen(
            onBack = { navController.popBackStack() },
            onEdit = { workerId -> navController.navigate(Routes.workerFormEdit(workerId)) },
            onWorkOrderClick = { workOrderId ->
                navController.navigate(Routes.workOrderDetail(workOrderId))
            },
        )
    }

    composable(Routes.WORKER_FORM) {
        WorkerFormScreen(
            onBack = { navController.popBackStack() },
            onSaved = { navController.popBackStack() },
        )
    }

    composable(
        route = Routes.WORKER_FORM_EDIT,
        arguments = listOf(navArgument(Routes.WORKER_ID_ARG) { type = NavType.StringType }),
    ) {
        WorkerFormScreen(
            onBack = { navController.popBackStack() },
            onSaved = { navController.popBackStack() },
        )
    }

    composable(Routes.ATTENDANCE) {
        AttendanceScreen(onBack = { navController.popBackStack() })
    }

    composable(Routes.WORK_ORDERS) {
        WorkOrderListScreen(
            onBack = { navController.popBackStack() },
            onWorkOrderClick = { id -> navController.navigate(Routes.workOrderDetail(id)) },
            onAddWorkOrder = { navController.navigate(Routes.WORK_ORDER_FORM) },
        )
    }

    composable(Routes.WORK_ORDER_FORM) {
        WorkOrderFormScreen(
            onBack = { navController.popBackStack() },
            onSaved = { navController.popBackStack() },
        )
    }

    composable(
        route = Routes.WORK_ORDER_DETAIL,
        arguments = listOf(navArgument(Routes.WORK_ORDER_ID_ARG) { type = NavType.StringType }),
    ) {
        WorkOrderDetailScreen(onBack = { navController.popBackStack() })
    }
}
