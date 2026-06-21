package com.krishifarms.mobile.core.security.rbac

import com.krishifarms.mobile.core.navigation.Routes
import com.krishifarms.mobile.core.security.session.UserContext
import com.krishifarms.mobile.feature.auth.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NavigationGuardTest {

    private lateinit var permissionManager: PermissionManagerImpl
    private lateinit var navigationGuard: NavigationGuard

    @Before
    fun setUp() {
        permissionManager = PermissionManagerImpl()
        navigationGuard = NavigationGuard(permissionManager)
    }

    @Test
    fun canAccess_allowsDashboardWithoutExtraPermissions() {
        permissionManager.updateContext(userContext(emptySet()))
        assertTrue(navigationGuard.canAccess(Routes.DASHBOARD))
    }

    @Test
    fun canAccess_blocksFarmersWithoutViewPermission() {
        permissionManager.updateContext(userContext(emptySet()))
        assertFalse(navigationGuard.canAccess(Routes.FARMERS))
    }

    @Test
    fun canAccess_allowsFarmersWithViewPermission() {
        permissionManager.updateContext(userContext(setOf(Permission.FARMER_VIEW)))
        assertTrue(navigationGuard.canAccess(Routes.FARMERS))
    }

    @Test
    fun fallbackRoute_isDashboard() {
        assertEquals(Routes.DASHBOARD, navigationGuard.fallbackRoute())
    }

    private fun userContext(permissions: Set<Permission>): UserContext = UserContext(
        user = User(id = "1", name = "Test", mobile = "9876543210", email = null, role = null),
        roles = emptySet(),
        permissions = permissions,
        accessibleModules = setOf("dashboard"),
    )
}
