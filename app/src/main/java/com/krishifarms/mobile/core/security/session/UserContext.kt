package com.krishifarms.mobile.core.security.session

import com.krishifarms.mobile.core.security.rbac.Permission
import com.krishifarms.mobile.feature.auth.domain.model.User

enum class SessionSource {
    LOGIN,
    REFRESH,
    RESTORE,
}

data class UserContext(
    val user: User,
    val roles: Set<String>,
    val permissions: Set<Permission>,
    val accessibleModules: Set<String>,
    val loadedAtMillis: Long = System.currentTimeMillis(),
    val source: SessionSource = SessionSource.LOGIN,
) {
    val isAuthenticated: Boolean get() = true
}
