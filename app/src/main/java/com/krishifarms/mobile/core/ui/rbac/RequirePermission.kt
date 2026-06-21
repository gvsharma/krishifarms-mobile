package com.krishifarms.mobile.core.ui.rbac

import androidx.compose.runtime.Composable
import com.krishifarms.mobile.core.security.rbac.Permission
import com.krishifarms.mobile.core.security.rbac.PermissionManager

@Composable
fun RequirePermission(
    permissionManager: PermissionManager,
    permission: Permission,
    content: @Composable () -> Unit,
) {
    if (permissionManager.has(permission)) {
        content()
    }
}

@Composable
fun RequireAnyPermission(
    permissionManager: PermissionManager,
    permissions: Set<Permission>,
    content: @Composable () -> Unit,
) {
    if (permissionManager.hasAny(*permissions.toTypedArray())) {
        content()
    }
}
