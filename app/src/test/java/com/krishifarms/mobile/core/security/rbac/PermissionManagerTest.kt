package com.krishifarms.mobile.core.security.rbac

import com.krishifarms.mobile.core.security.session.UserContext
import com.krishifarms.mobile.feature.auth.domain.model.User
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PermissionManagerTest {

    private lateinit var permissionManager: PermissionManagerImpl

    @Before
    fun setUp() {
        permissionManager = PermissionManagerImpl()
    }

    @Test
    fun has_returnsTrueWhenPermissionGranted() {
        permissionManager.updateContext(
            userContext(setOf(Permission.FARMER_VIEW, Permission.FARMER_CREATE)),
        )
        assertTrue(permissionManager.has(Permission.FARMER_VIEW))
        assertTrue(permissionManager.has(Permission.FARMER_CREATE))
    }

    @Test
    fun has_returnsFalseWhenPermissionMissing() {
        permissionManager.updateContext(userContext(setOf(Permission.FARMER_VIEW)))
        assertFalse(permissionManager.has(Permission.FARMER_CREATE))
    }

    @Test
    fun hasAny_returnsTrueWhenOneMatches() {
        permissionManager.updateContext(userContext(setOf(Permission.EXPENSE_VIEW)))
        assertTrue(permissionManager.hasAny(Permission.EXPENSE_VIEW, Permission.EXPENSE_CREATE))
    }

    @Test
    fun canAccessModule_checksAccessibleModules() {
        permissionManager.updateContext(
            userContext(
                permissions = setOf(Permission.FARMER_VIEW),
                modules = setOf("farmers", "dashboard"),
            ),
        )
        assertTrue(permissionManager.canAccessModule("farmers"))
        assertFalse(permissionManager.canAccessModule("expenses"))
    }

    private fun userContext(
        permissions: Set<Permission>,
        modules: Set<String> = setOf("dashboard"),
    ): UserContext = UserContext(
        user = User(id = "1", name = "Test", mobile = "9876543210", email = null, role = "MANAGER"),
        roles = setOf("MANAGER"),
        permissions = permissions,
        accessibleModules = modules,
    )
}
