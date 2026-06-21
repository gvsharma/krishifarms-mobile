package com.krishifarms.mobile.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.krishifarms.mobile.feature.auth.presentation.AuthViewModel
import com.krishifarms.mobile.feature.auth.presentation.SessionState
import com.krishifarms.mobile.feature.auth.presentation.login.LoginScreen

@Composable
fun KrishiFarmsNavHost(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val sessionState by authViewModel.sessionState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionState) {
        when (sessionState) {
            SessionState.Loading -> navController.navigate(Routes.SESSION_LOADING) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }

            SessionState.Unauthenticated -> navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }

            SessionState.Authenticated -> navController.navigate(Routes.MAIN_GRAPH) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SESSION_LOADING,
        modifier = modifier,
    ) {
        composable(Routes.SESSION_LOADING) {
            SessionLoadingScreen()
        }

        composable(Routes.LOGIN) {
            LoginScreen(viewModel = authViewModel)
        }

        composable(Routes.MAIN_GRAPH) {
            MainNavGraph(onLogout = authViewModel::logout)
        }
    }
}

@Composable
private fun SessionLoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
