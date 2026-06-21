package com.krishifarms.mobile.core.security.rbac

import com.krishifarms.mobile.core.security.session.UserContext
import com.krishifarms.mobile.feature.auth.domain.model.User
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicMenuProviderTest {

    private val provider = DynamicMenuProvider()

    @Test
    fun visibleEntries_alwaysIncludesDashboard() {
        val entries = provider.visibleEntries(
            UserContext(
                user = User(id = "1", name = "Worker", mobile = "9876543210", email = null, role = "WORKER"),
                roles = setOf("WORKER"),
                permissions = setOf(Permission.WORK_ORDER_VIEW),
                accessibleModules = setOf("dashboard", "work_orders"),
            ),
        )
        assertTrue(entries.any { it.moduleId == "dashboard" })
        assertTrue(entries.any { it.moduleId == "work_orders" })
        assertFalse(entries.any { it.moduleId == "farmers" })
    }
}
