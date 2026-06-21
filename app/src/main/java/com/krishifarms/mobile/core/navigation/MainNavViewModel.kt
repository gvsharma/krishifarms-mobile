package com.krishifarms.mobile.core.navigation

import androidx.lifecycle.ViewModel
import com.krishifarms.mobile.core.security.rbac.DynamicMenuProvider
import com.krishifarms.mobile.core.security.rbac.NavigationGuard
import com.krishifarms.mobile.core.security.rbac.PermissionManager
import com.krishifarms.mobile.core.security.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainNavViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val permissionManager: PermissionManager,
    val navigationGuard: NavigationGuard,
    val dynamicMenuProvider: DynamicMenuProvider,
) : ViewModel()
