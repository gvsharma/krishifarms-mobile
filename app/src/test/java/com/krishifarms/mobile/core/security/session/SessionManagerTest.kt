package com.krishifarms.mobile.core.security.session

import com.krishifarms.mobile.core.data.local.entity.UserSessionEntity
import com.krishifarms.mobile.core.database.dao.UserSessionDao
import com.krishifarms.mobile.core.security.rbac.Permission
import com.krishifarms.mobile.core.security.rbac.PermissionManagerImpl
import com.krishifarms.mobile.feature.auth.data.mapper.SessionMapper
import com.krishifarms.mobile.feature.auth.data.dto.TokenResponse
import com.krishifarms.mobile.feature.auth.data.dto.UserDto
import com.krishifarms.mobile.feature.auth.domain.model.UserSession
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SessionManagerTest {

    private lateinit var dao: FakeUserSessionDao
    private lateinit var permissionManager: PermissionManagerImpl
    private lateinit var sessionMapper: SessionMapper
    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        dao = FakeUserSessionDao()
        permissionManager = PermissionManagerImpl()
        sessionMapper = SessionMapper(Json { ignoreUnknownKeys = true })
        sessionManager = SessionManager(dao, permissionManager, sessionMapper)
    }

    @Test
    fun updateFromLogin_publishesPermissions() = runTest {
        sessionManager.updateFromLogin(
            TokenResponse(
                accessToken = "a",
                refreshToken = "r",
                user = UserDto(id = "u1", name = "Venkat", mobile = "9876543210"),
                roles = listOf("MANAGER"),
                permissions = listOf(Permission.FARMER_VIEW.code),
                accessibleModules = listOf("dashboard", "farmers"),
            ),
        )

        val context = sessionManager.session.value
        assertNotNull(context)
        assertEquals("Venkat", context?.user?.name)
        org.junit.Assert.assertTrue(context!!.permissions.contains(Permission.FARMER_VIEW))
    }

    @Test
    fun restore_returnsNullWhenNoSession() = runTest {
        assertNull(sessionManager.restore())
    }

    @Test
    fun clear_wipesSession() = runTest {
        sessionManager.updateFromUserSession(
            UserSession(
                user = com.krishifarms.mobile.feature.auth.domain.model.User(
                    id = "u1",
                    name = "Test",
                    mobile = "9876543210",
                    email = null,
                    role = null,
                ),
                roles = emptySet(),
                permissions = setOf(Permission.FARMER_VIEW),
                accessibleModules = setOf("dashboard"),
            ),
        )
        sessionManager.clear()
        assertNull(sessionManager.session.value)
        assertNull(dao.getSession())
    }

    private class FakeUserSessionDao : UserSessionDao {
        private var entity: UserSessionEntity? = null

        override suspend fun upsert(session: UserSessionEntity) {
            entity = session
        }

        override suspend fun getSession(): UserSessionEntity? = entity

        override suspend fun clear() {
            entity = null
        }
    }
}
