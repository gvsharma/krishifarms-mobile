package com.krishifarms.mobile.feature.farmer.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.krishifarms.mobile.core.navigation.Routes
import com.krishifarms.mobile.feature.farmer.presentation.detail.FarmerDetailScreen
import com.krishifarms.mobile.feature.farmer.presentation.form.FarmerFormScreen
import com.krishifarms.mobile.feature.farmer.presentation.list.FarmerListScreen

fun NavGraphBuilder.farmerGraph(navController: NavController) {
    composable(Routes.FARMERS) {
        FarmerListScreen(
            onBack = { navController.popBackStack() },
            onAddFarmer = { navController.navigate(Routes.FARMER_ADD) },
            onFarmerClick = { farmerId ->
                navController.navigate(Routes.farmerDetail(farmerId))
            },
        )
    }

    composable(
        route = Routes.FARMER_DETAIL,
        arguments = listOf(navArgument(Routes.FARMER_ID_ARG) { type = NavType.StringType }),
    ) {
        FarmerDetailScreen(
            onBack = { navController.popBackStack() },
            onEdit = { farmerId -> navController.navigate(Routes.farmerEdit(farmerId)) },
        )
    }

    composable(Routes.FARMER_ADD) {
        FarmerFormScreen(
            onBack = { navController.popBackStack() },
            onSaved = { navController.popBackStack() },
        )
    }

    composable(
        route = Routes.FARMER_EDIT,
        arguments = listOf(navArgument(Routes.FARMER_ID_ARG) { type = NavType.StringType }),
    ) {
        FarmerFormScreen(
            onBack = { navController.popBackStack() },
            onSaved = { navController.popBackStack() },
        )
    }
}
