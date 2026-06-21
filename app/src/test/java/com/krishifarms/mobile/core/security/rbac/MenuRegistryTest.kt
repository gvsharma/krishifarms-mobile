package com.krishifarms.mobile.core.security.rbac

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuRegistryTest {

    @Test
    fun allModuleIds_includesDashboardAndSettings() {
        val ids = MenuRegistry.allModuleIds()
        assertTrue(ids.contains("dashboard"))
        assertTrue(ids.contains("settings"))
    }

    @Test
    fun viewPermissionsForModule_returnsFarmerViewForFarmers() {
        assertEquals(listOf(Permission.FARMER_VIEW.code), MenuRegistry.viewPermissionsForModule("farmers"))
    }

    @Test
    fun entryForRoute_matchesNestedRoutes() {
        val entry = MenuRegistry.entryForRoute("farmers/abc-123")
        assertEquals("farmers", entry?.moduleId)
    }
}
