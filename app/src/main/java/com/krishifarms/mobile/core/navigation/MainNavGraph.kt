package com.krishifarms.mobile.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.krishifarms.mobile.R
import com.krishifarms.mobile.feature.dashboard.presentation.DashboardScreen
import com.krishifarms.mobile.feature.expense.presentation.detail.ExpenseDetailScreen
import com.krishifarms.mobile.feature.expense.presentation.form.ExpenseFormScreen
import com.krishifarms.mobile.feature.expense.presentation.list.ExpenseListScreen
import com.krishifarms.mobile.feature.farmer.navigation.farmerGraph
import com.krishifarms.mobile.feature.procurement.presentation.detail.ProcurementDetailScreen
import com.krishifarms.mobile.feature.procurement.presentation.form.ProcurementFormScreen
import com.krishifarms.mobile.feature.procurement.presentation.list.ProcurementListScreen
import com.krishifarms.mobile.feature.worker.navigation.workerRoutes
import com.krishifarms.mobile.feature.document.navigation.DocumentRoutes
import com.krishifarms.mobile.feature.document.navigation.documentRoutes
import com.krishifarms.mobile.feature.document.presentation.list.DocumentListScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavGraph(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentDestination = (MainFeatureDestinations + SyncDestination)
        .firstOrNull { it.route == currentRoute || currentRoute?.startsWith(it.route) == true }
        ?: MainFeatureDestinations.first()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                )
                (MainFeatureDestinations + SyncDestination).forEach { destination ->
                    NavigationDrawerItem(
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(stringResource(destination.titleRes)) },
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )
                }
            }
        },
        modifier = modifier,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(currentDestination.titleRes)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.common_menu))
                        }
                    },
                )
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.DASHBOARD,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(Routes.DASHBOARD) {
                    DashboardScreen(
                        onCardClick = { cardType ->
                            navController.navigate(cardType.toRoute())
                        },
                    )
                }

                farmerGraph(navController)
                workerRoutes(navController)

                composable(Routes.FARMS) {
                    FeatureStubScreen(
                        title = stringResource(R.string.farms_title),
                        subtitle = stringResource(R.string.farms_subtitle),
                        onNavigateBack = { navController.popBackStack() },
                    )
                }

                composable(ProcurementRoutes.LIST) {
                    ProcurementListScreen(
                        onBack = { navController.popBackStack() },
                        onCreateClick = { navController.navigate(ProcurementRoutes.CREATE) },
                        onProcurementClick = { id ->
                            navController.navigate(ProcurementRoutes.detail(id))
                        },
                    )
                }

                composable(
                    route = ProcurementRoutes.DETAIL,
                    arguments = listOf(
                        navArgument(ProcurementRoutes.ARG_PROCUREMENT_ID) { type = NavType.StringType },
                    ),
                ) {
                    ProcurementDetailScreen(onBack = { navController.popBackStack() })
                }

                composable(ProcurementRoutes.CREATE) {
                    ProcurementFormScreen(
                        onBack = { navController.popBackStack() },
                        onSaved = { id ->
                            navController.popBackStack()
                            navController.navigate(ProcurementRoutes.detail(id))
                        },
                    )
                }

                composable(Routes.PROCUREMENT) {
                    ProcurementListScreen(
                        onBack = { navController.popBackStack() },
                        onCreateClick = { navController.navigate(ProcurementRoutes.CREATE) },
                        onProcurementClick = { id ->
                            navController.navigate(ProcurementRoutes.detail(id))
                        },
                    )
                }

                composable(Routes.FARMER_PAYMENTS) {
                    FeatureStubScreen(
                        title = stringResource(R.string.farmer_payments_title),
                        subtitle = stringResource(R.string.farmer_payments_subtitle),
                        onNavigateBack = { navController.popBackStack() },
                    )
                }

                composable(ExpenseRoutes.LIST) {
                    ExpenseListScreen(
                        onBack = { navController.popBackStack() },
                        onCreateClick = { navController.navigate(ExpenseRoutes.FORM) },
                        onExpenseClick = { id ->
                            navController.navigate(ExpenseRoutes.detail(id))
                        },
                    )
                }

                composable(
                    route = ExpenseRoutes.DETAIL,
                    arguments = listOf(
                        navArgument(ExpenseRoutes.ARG_EXPENSE_ID) { type = NavType.StringType },
                    ),
                ) {
                    ExpenseDetailScreen(onBack = { navController.popBackStack() })
                }

                composable(ExpenseRoutes.FORM) {
                    ExpenseFormScreen(
                        onBack = { navController.popBackStack() },
                        onSaved = { id ->
                            navController.popBackStack()
                            navController.navigate(ExpenseRoutes.detail(id))
                        },
                    )
                }

                composable(Routes.EXPENSES) {
                    ExpenseListScreen(
                        onBack = { navController.popBackStack() },
                        onCreateClick = { navController.navigate(ExpenseRoutes.FORM) },
                        onExpenseClick = { id ->
                            navController.navigate(ExpenseRoutes.detail(id))
                        },
                    )
                }

                composable(Routes.COLLECTIONS) {
                    FeatureStubScreen(
                        title = stringResource(R.string.collections_title),
                        subtitle = stringResource(R.string.collections_subtitle),
                        onNavigateBack = { navController.popBackStack() },
                    )
                }

                composable(Routes.PAYMENTS) {
                    FeatureStubScreen(
                        title = stringResource(R.string.payments_title),
                        subtitle = stringResource(R.string.payments_subtitle),
                        onNavigateBack = { navController.popBackStack() },
                    )
                }

                composable(Routes.VEHICLES) {
                    FeatureStubScreen(
                        title = stringResource(R.string.vehicles_title),
                        subtitle = stringResource(R.string.vehicles_subtitle),
                        onNavigateBack = { navController.popBackStack() },
                    )
                }

                composable(Routes.VEHICLE_TRIPS) {
                    FeatureStubScreen(
                        title = stringResource(R.string.vehicle_trips_title),
                        subtitle = stringResource(R.string.vehicle_trips_subtitle),
                        onNavigateBack = { navController.popBackStack() },
                    )
                }

                composable(Routes.ASSETS) {
                    FeatureStubScreen(
                        title = stringResource(R.string.assets_title),
                        subtitle = stringResource(R.string.assets_subtitle),
                        onNavigateBack = { navController.popBackStack() },
                    )
                }

                composable(Routes.DOCUMENTS) {
                    DocumentListScreen(
                        onBack = { navController.popBackStack() },
                        onUploadClick = { navController.navigate(DocumentRoutes.upload()) },
                        onDocumentClick = { documentId ->
                            navController.navigate(DocumentRoutes.preview(documentId))
                        },
                    )
                }

                documentRoutes(navController)

                composable(Routes.RENTALS) {
                    FeatureStubScreen(
                        title = stringResource(R.string.rentals_title),
                        subtitle = stringResource(R.string.rentals_subtitle),
                        onNavigateBack = { navController.popBackStack() },
                    )
                }

                composable(Routes.SETTINGS) {
                    FeatureStubScreen(
                        title = stringResource(R.string.settings_title),
                        subtitle = stringResource(R.string.settings_subtitle),
                        onLogout = onLogout,
                    )
                }

                composable(Routes.SYNC) {
                    FeatureStubScreen(
                        title = stringResource(R.string.sync_title),
                        subtitle = stringResource(R.string.sync_subtitle),
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
