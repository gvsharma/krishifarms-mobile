package com.krishifarms.mobile.feature.auth.data.mapper

import com.krishifarms.mobile.core.data.local.entity.UserSessionEntity
import com.krishifarms.mobile.core.security.rbac.Permission
import com.krishifarms.mobile.core.security.rbac.RbacConfig
import com.krishifarms.mobile.core.security.session.SessionSource
import com.krishifarms.mobile.core.security.session.UserContext
import com.krishifarms.mobile.feature.auth.data.dto.TokenResponse
import com.krishifarms.mobile.feature.auth.data.dto.UserDto
import com.krishifarms.mobile.feature.auth.domain.model.User
import com.krishifarms.mobile.feature.auth.domain.model.UserSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionMapper @Inject constructor(
    private val json: Json,
) {
    fun fromTokenResponse(response: TokenResponse, source: SessionSource): UserSession {
        val user = response.user?.toDomain()
            ?: User(id = "", name = "", mobile = "", email = null, role = null)
        val permissions = Permission.fromCodes(response.permissions)
        val modules = response.accessibleModules.toSet()
        return UserSession(
            user = user,
            roles = response.roles.toSet(),
            permissions = RbacConfig.effectivePermissions(permissions, modules),
            accessibleModules = RbacConfig.effectiveModules(permissions, modules),
            source = source,
        )
    }

    fun fromEntity(entity: UserSessionEntity): UserSession = UserSession(
        user = User(
            id = entity.userId,
            name = entity.name,
            mobile = entity.mobile,
            email = entity.email,
            role = entity.role,
        ),
        roles = decodeStringSet(entity.rolesJson),
        permissions = Permission.fromCodes(decodeStringSet(entity.permissionsJson)),
        accessibleModules = decodeStringSet(entity.accessibleModulesJson),
        loadedAtMillis = entity.lastLoginAt,
        source = SessionSource.RESTORE,
    )

    fun toEntity(session: UserSession): UserSessionEntity = UserSessionEntity(
        userId = session.user.id,
        name = session.user.name,
        mobile = session.user.mobile,
        email = session.user.email,
        role = session.user.role,
        rolesJson = encodeStringSet(session.roles),
        permissionsJson = encodeStringSet(session.permissions.map { it.code }),
        accessibleModulesJson = encodeStringSet(session.accessibleModules),
        lastLoginAt = session.loadedAtMillis,
    )

    fun toUserContext(session: UserSession, source: SessionSource): UserContext {
        val permissions = RbacConfig.effectivePermissions(session.permissions, session.accessibleModules)
        val modules = RbacConfig.effectiveModules(session.permissions, session.accessibleModules)
        return UserContext(
            user = session.user,
            roles = session.roles,
            permissions = permissions,
            accessibleModules = modules,
            loadedAtMillis = session.loadedAtMillis,
            source = source,
        )
    }

    private fun encodeStringSet(values: Collection<String>): String =
        json.encodeToString(values.sorted())

    private fun decodeStringSet(raw: String?): Set<String> {
        if (raw.isNullOrBlank()) return emptySet()
        return runCatching { json.decodeFromString<List<String>>(raw).toSet() }.getOrDefault(emptySet())
    }

    private fun UserDto.toDomain(): User = User(
        id = id,
        name = name,
        mobile = mobile,
        email = email,
        role = role,
    )
}
