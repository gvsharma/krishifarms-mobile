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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.krishifarms.mobile.R
import com.krishifarms.mobile.core.security.rbac.GuardedRoute
import com.krishifarms.mobile.core.security.rbac.MenuEntry
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
    shellViewModel: MainNavViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val forbiddenMessage = stringResource(R.string.rbac_route_forbidden)

    val session by shellViewModel.sessionManager.session.collectAsStateWithLifecycle()
    val permissionManager = shellViewModel.permissionManager
    val navigationGuard = shellViewModel.navigationGuard

    val menuEntries = session?.let { shellViewModel.dynamicMenuProvider.visibleEntries(it) }
        ?: emptyList()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentMenuEntry = menuEntries.firstOrNull { entry ->
        currentRoute == entry.route || currentRoute?.startsWith("${entry.route}/") == true
    } ?: menuEntries.firstOrNull()

    fun onForbidden() {
        scope.launch { snackbarHostState.showSnackbar(forbiddenMessage) }
    }

    fun guardedNavigate(route: String) {
        navController.guardedNavigate(route, navigationGuard, ::onForbidden) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun goDashboard() {
        navController.navigate(Routes.DASHBOARD) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                )
                menuEntries.forEach { entry ->
                    DrawerMenuItem(
                        entry = entry,
                        selected = currentRoute == entry.route,
                        onClick = {
                            guardedNavigate(entry.route)
                            scope.launch { drawerState.close() }
                        },
                    )
                }
            }
        },
        modifier = modifier,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            currentMenuEntry?.titleRes?.let { stringResource(it) }
                                ?: stringResource(R.string.app_name),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.common_menu))
                        }
                    },
                )
            },
            bottomBar = {
                MainBottomNav(
                    navController = navController,
                    userContext = session,
                    navigationGuard = navigationGuard,
                    onForbidden = ::onForbidden,
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.DASHBOARD,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(Routes.DASHBOARD) {
                    DashboardScreen(
                        onCardClick = { cardType -> guardedNavigate(cardType.toRoute()) },
                    )
                }

                farmerGraph(
                    navController = navController,
                    permissionManager = permissionManager,
                    onNavigateToDashboard = ::goDashboard,
                    guardedNavigate = ::guardedNavigate,
                )
                workerRoutes(
                    navController = navController,
                    permissionManager = permissionManager,
                    onNavigateToDashboard = ::goDashboard,
                    guardedNavigate = ::guardedNavigate,
                )

                composable(Routes.FARMS) {
                    GuardedRoute(Routes.FARMS, permissionManager, ::goDashboard) {
                        FeatureStubScreen(
                            title = stringResource(R.string.farms_title),
                            subtitle = stringResource(R.string.farms_subtitle),
                            onNavigateBack = { navController.popBackStack() },
                        )
                    }
                }

                composable(ProcurementRoutes.LIST) {
                    GuardedRoute(ProcurementRoutes.LIST, permissionManager, ::goDashboard) {
                        ProcurementListScreen(
                            onBack = { navController.popBackStack() },
                            onCreateClick = { guardedNavigate(ProcurementRoutes.CREATE) },
                            onProcurementClick = { id -> guardedNavigate(ProcurementRoutes.detail(id)) },
                        )
                    }
                }

                composable(
                    route = ProcurementRoutes.DETAIL,
                    arguments = listOf(
                        navArgument(ProcurementRoutes.ARG_PROCUREMENT_ID) { type = NavType.StringType },
                    ),
                ) {
                    GuardedRoute(ProcurementRoutes.DETAIL, permissionManager, ::goDashboard) {
                        ProcurementDetailScreen(onBack = { navController.popBackStack() })
                    }
                }

                composable(ProcurementRoutes.CREATE) {
                    GuardedRoute(ProcurementRoutes.CREATE, permissionManager, ::goDashboard) {
                        ProcurementFormScreen(
                            onBack = { navController.popBackStack() },
                            onSaved = { id ->
                                navController.popBackStack()
                                guardedNavigate(ProcurementRoutes.detail(id))
                            },
                        )
                    }
                }

                composable(Routes.PROCUREMENT) {
                    GuardedRoute(Routes.PROCUREMENT, permissionManager, ::goDashboard) {
                        ProcurementListScreen(
                            onBack = { navController.popBackStack() },
                            onCreateClick = { guardedNavigate(ProcurementRoutes.CREATE) },
                            onProcurementClick = { id -> guardedNavigate(ProcurementRoutes.detail(id)) },
                        )
                    }
                }

                composable(Routes.FARMER_PAYMENTS) {
                    GuardedRoute(Routes.FARMER_PAYMENTS, permissionManager, ::goDashboard) {
                        FeatureStubScreen(
                            title = stringResource(R.string.farmer_payments_title),
                            subtitle = stringResource(R.string.farmer_payments_subtitle),
                            onNavigateBack = { navController.popBackStack() },
                        )
                    }
                }

                composable(ExpenseRoutes.LIST) {
                    GuardedRoute(ExpenseRoutes.LIST, permissionManager, ::goDashboard) {
                        ExpenseListScreen(
                            onBack = { navController.popBackStack() },
                            onCreateClick = { guardedNavigate(ExpenseRoutes.FORM) },
                            onExpenseClick = { id -> guardedNavigate(ExpenseRoutes.detail(id)) },
                        )
                    }
                }

                composable(
                    route = ExpenseRoutes.DETAIL,
                    arguments = listOf(
                        navArgument(ExpenseRoutes.ARG_EXPENSE_ID) { type = NavType.StringType },
                    ),
                ) {
                    GuardedRoute(ExpenseRoutes.DETAIL, permissionManager, ::goDashboard) {
                        ExpenseDetailScreen(onBack = { navController.popBackStack() })
                    }
                }

                composable(ExpenseRoutes.FORM) {
                    GuardedRoute(ExpenseRoutes.FORM, permissionManager, ::goDashboard) {
                        ExpenseFormScreen(
                            onBack = { navController.popBackStack() },
                            onSaved = { id ->
                                navController.popBackStack()
                                guardedNavigate(ExpenseRoutes.detail(id))
                            },
                        )
                    }
                }

                composable(Routes.EXPENSES) {
                    GuardedRoute(Routes.EXPENSES, permissionManager, ::goDashboard) {
                        ExpenseListScreen(
                            onBack = { navController.popBackStack() },
                            onCreateClick = { guardedNavigate(ExpenseRoutes.FORM) },
                            onExpenseClick = { id -> guardedNavigate(ExpenseRoutes.detail(id)) },
                        )
                    }
                }

                composable(Routes.COLLECTIONS) {
                    GuardedRoute(Routes.COLLECTIONS, permissionManager, ::goDashboard) {
                        FeatureStubScreen(
                            title = stringResource(R.string.collections_title),
                            subtitle = stringResource(R.string.collections_subtitle),
                            onNavigateBack = { navController.popBackStack() },
                        )
                    }
                }

                composable(Routes.PAYMENTS) {
                    GuardedRoute(Routes.PAYMENTS, permissionManager, ::goDashboard) {
                        FeatureStubScreen(
                            title = stringResource(R.string.payments_title),
                            subtitle = stringResource(R.string.payments_subtitle),
                            onNavigateBack = { navController.popBackStack() },
                        )
                    }
                }

                composable(Routes.VEHICLES) {
                    GuardedRoute(Routes.VEHICLES, permissionManager, ::goDashboard) {
                        FeatureStubScreen(
                            title = stringResource(R.string.vehicles_title),
                            subtitle = stringResource(R.string.vehicles_subtitle),
                            onNavigateBack = { navController.popBackStack() },
                        )
                    }
                }

                composable(Routes.VEHICLE_TRIPS) {
                    GuardedRoute(Routes.VEHICLE_TRIPS, permissionManager, ::goDashboard) {
                        FeatureStubScreen(
                            title = stringResource(R.string.vehicle_trips_title),
                            subtitle = stringResource(R.string.vehicle_trips_subtitle),
                            onNavigateBack = { navController.popBackStack() },
                        )
                    }
                }

                composable(Routes.ASSETS) {
                    GuardedRoute(Routes.ASSETS, permissionManager, ::goDashboard) {
                        FeatureStubScreen(
                            title = stringResource(R.string.assets_title),
                            subtitle = stringResource(R.string.assets_subtitle),
                            onNavigateBack = { navController.popBackStack() },
                        )
                    }
                }

                composable(Routes.DOCUMENTS) {
                    GuardedRoute(Routes.DOCUMENTS, permissionManager, ::goDashboard) {
                        DocumentListScreen(
                            onBack = { navController.popBackStack() },
                            onUploadClick = { guardedNavigate(DocumentRoutes.upload()) },
                            onDocumentClick = { documentId ->
                                guardedNavigate(DocumentRoutes.preview(documentId))
                            },
                        )
                    }
                }

                documentRoutes(
                    navController = navController,
                    permissionManager = permissionManager,
                    onNavigateToDashboard = ::goDashboard,
                    guardedNavigate = ::guardedNavigate,
                )

                composable(Routes.RENTALS) {
                    GuardedRoute(Routes.RENTALS, permissionManager, ::goDashboard) {
                        FeatureStubScreen(
                            title = stringResource(R.string.rentals_title),
                            subtitle = stringResource(R.string.rentals_subtitle),
                            onNavigateBack = { navController.popBackStack() },
                        )
                    }
                }

                composable(Routes.SETTINGS) {
                    GuardedRoute(Routes.SETTINGS, permissionManager, ::goDashboard) {
                        FeatureStubScreen(
                            title = stringResource(R.string.settings_title),
                            subtitle = stringResource(R.string.settings_subtitle),
                            onLogout = onLogout,
                        )
                    }
                }

                composable(Routes.SYNC) {
                    GuardedRoute(Routes.SYNC, permissionManager, ::goDashboard) {
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
}

@Composable
private fun DrawerMenuItem(
    entry: MenuEntry,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        icon = { Icon(entry.icon, contentDescription = null) },
        label = { Text(stringResource(entry.titleRes)) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
    )
}
