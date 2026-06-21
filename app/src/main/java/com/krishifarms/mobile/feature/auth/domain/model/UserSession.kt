package com.krishifarms.mobile.feature.auth.domain.model

import com.krishifarms.mobile.core.security.rbac.Permission
import com.krishifarms.mobile.core.security.session.SessionSource

data class UserSession(
    val user: User,
    val roles: Set<String>,
    val permissions: Set<Permission>,
    val accessibleModules: Set<String>,
    val loadedAtMillis: Long = System.currentTimeMillis(),
    val source: SessionSource = SessionSource.LOGIN,
)
