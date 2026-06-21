package com.krishifarms.mobile.core.security.rbac

class PermissionDeniedException(
    message: String = "Permission denied",
) : Exception(message)

object PermissionGuard {
    fun require(permissionManager: PermissionManager, permission: Permission) {
        if (!permissionManager.has(permission)) {
            throw PermissionDeniedException("Missing permission: ${permission.code}")
        }
    }
}
