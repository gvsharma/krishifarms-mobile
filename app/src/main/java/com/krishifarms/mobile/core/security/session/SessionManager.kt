package com.krishifarms.mobile.core.security.session

import com.krishifarms.mobile.core.data.local.entity.UserSessionEntity
import com.krishifarms.mobile.core.database.dao.UserSessionDao
import com.krishifarms.mobile.core.security.rbac.PermissionManager
import com.krishifarms.mobile.core.security.rbac.RbacConfig
import com.krishifarms.mobile.feature.auth.data.dto.TokenResponse
import com.krishifarms.mobile.feature.auth.data.mapper.SessionMapper
import com.krishifarms.mobile.feature.auth.domain.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val userSessionDao: UserSessionDao,
    private val permissionManager: PermissionManager,
    private val sessionMapper: SessionMapper,
) {
    private val _session = MutableStateFlow<UserContext?>(null)
    val session: StateFlow<UserContext?> = _session.asStateFlow()

    suspend fun updateFromLogin(response: TokenResponse, source: SessionSource = SessionSource.LOGIN) {
        val userSession = sessionMapper.fromTokenResponse(response, source)
        persistAndPublish(userSession, source)
    }

    suspend fun updateFromUserSession(userSession: UserSession, source: SessionSource = SessionSource.RESTORE) {
        persistAndPublish(userSession, source)
    }

    suspend fun restore(): UserContext? {
        val entity = userSessionDao.getSession() ?: run {
            clear()
            return null
        }
        val userSession = sessionMapper.fromEntity(entity)
        val context = toContext(userSession, SessionSource.RESTORE)
        applyContext(context)
        return context
    }

    suspend fun clear() {
        userSessionDao.clear()
        applyContext(null)
    }

    private suspend fun persistAndPublish(userSession: UserSession, source: SessionSource) {
        userSessionDao.upsert(sessionMapper.toEntity(userSession))
        applyContext(toContext(userSession, source))
    }

    private fun toContext(userSession: UserSession, source: SessionSource): UserContext {
        val permissions = RbacConfig.effectivePermissions(userSession.permissions, userSession.accessibleModules)
        val modules = RbacConfig.effectiveModules(userSession.permissions, userSession.accessibleModules)
        return UserContext(
            user = userSession.user,
            roles = userSession.roles,
            permissions = permissions,
            accessibleModules = modules,
            loadedAtMillis = userSession.loadedAtMillis,
            source = source,
        )
    }

    private fun applyContext(context: UserContext?) {
        _session.value = context
        permissionManager.updateContext(context)
    }
}
