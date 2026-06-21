package com.krishifarms.mobile.core.security.rbac

import com.krishifarms.mobile.BuildConfig

object RbacConfig {
    val strictMode: Boolean = BuildConfig.RBAC_STRICT_MODE

    fun effectivePermissions(
        permissions: Set<Permission>,
        accessibleModules: Set<String>,
    ): Set<Permission> {
        if (permissions.isNotEmpty()) return permissions
        if (!strictMode) return Permission.all()
        return if (accessibleModules.isNotEmpty()) {
            derivePermissionsFromModules(accessibleModules)
        } else {
            setOf(Permission.REPORT_VIEW, Permission.SETTINGS_VIEW)
        }
    }

    fun effectiveModules(
        permissions: Set<Permission>,
        accessibleModules: Set<String>,
    ): Set<String> {
        if (accessibleModules.isNotEmpty()) return accessibleModules
        if (!strictMode) return MenuRegistry.allModuleIds()
        return setOf("dashboard", "settings")
    }

    private fun derivePermissionsFromModules(modules: Set<String>): Set<Permission> =
        modules.flatMap { moduleId ->
            MenuRegistry.viewPermissionsForModule(moduleId).mapNotNull { Permission.fromCode(it) }
        }.toSet() + Permission.REPORT_VIEW
}
