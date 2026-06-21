package com.krishifarms.mobile.feature.farmer.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.krishifarms.mobile.core.navigation.Routes
import com.krishifarms.mobile.core.security.rbac.GuardedRoute
import com.krishifarms.mobile.core.security.rbac.PermissionManager
import com.krishifarms.mobile.feature.farmer.presentation.detail.FarmerDetailScreen
import com.krishifarms.mobile.feature.farmer.presentation.form.FarmerFormScreen
import com.krishifarms.mobile.feature.farmer.presentation.list.FarmerListScreen

fun NavGraphBuilder.farmerGraph(
    navController: NavController,
    permissionManager: PermissionManager,
    onNavigateToDashboard: () -> Unit,
    guardedNavigate: (String) -> Unit,
) {
    composable(Routes.FARMERS) {
        GuardedRoute(Routes.FARMERS, permissionManager, onNavigateToDashboard) {
            FarmerListScreen(
                onBack = { navController.popBackStack() },
                onAddFarmer = { guardedNavigate(Routes.FARMER_ADD) },
                onFarmerClick = { farmerId -> guardedNavigate(Routes.farmerDetail(farmerId)) },
            )
        }
    }

    composable(
        route = Routes.FARMER_DETAIL,
        arguments = listOf(navArgument(Routes.FARMER_ID_ARG) { type = NavType.StringType }),
    ) {
        GuardedRoute(Routes.FARMER_DETAIL, permissionManager, onNavigateToDashboard) {
            FarmerDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { farmerId -> guardedNavigate(Routes.farmerEdit(farmerId)) },
            )
        }
    }

    composable(Routes.FARMER_ADD) {
        GuardedRoute(Routes.FARMER_ADD, permissionManager, onNavigateToDashboard) {
            FarmerFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
    }

    composable(
        route = Routes.FARMER_EDIT,
        arguments = listOf(navArgument(Routes.FARMER_ID_ARG) { type = NavType.StringType }),
    ) {
        GuardedRoute(Routes.FARMER_EDIT, permissionManager, onNavigateToDashboard) {
            FarmerFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
    }
}
