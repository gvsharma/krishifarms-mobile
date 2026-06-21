package com.krishifarms.mobile.core.security.rbac

import com.krishifarms.mobile.core.security.session.UserContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface PermissionManager {
    val permissions: StateFlow<Set<Permission>>
    val accessibleModules: StateFlow<Set<String>>
    val userContext: StateFlow<UserContext?>

    fun isAuthenticated(): Boolean
    fun has(permission: Permission): Boolean
    fun hasAny(vararg permissions: Permission): Boolean
    fun hasAll(vararg permissions: Permission): Boolean
    fun canAccessModule(moduleId: String): Boolean
    fun updateContext(context: UserContext?)
    fun clear()
}

@Singleton
class PermissionManagerImpl @Inject constructor() : PermissionManager {

    private val _userContext = MutableStateFlow<UserContext?>(null)
    override val userContext: StateFlow<UserContext?> = _userContext.asStateFlow()

    private val _permissions = MutableStateFlow<Set<Permission>>(emptySet())
    override val permissions: StateFlow<Set<Permission>> = _permissions.asStateFlow()

    private val _accessibleModules = MutableStateFlow<Set<String>>(emptySet())
    override val accessibleModules: StateFlow<Set<String>> = _accessibleModules.asStateFlow()

    override fun isAuthenticated(): Boolean = _userContext.value != null

    override fun has(permission: Permission): Boolean = _permissions.value.contains(permission)

    override fun hasAny(vararg permissions: Permission): Boolean =
        permissions.any { _permissions.value.contains(it) }

    override fun hasAll(vararg permissions: Permission): Boolean =
        permissions.all { _permissions.value.contains(it) }

    override fun canAccessModule(moduleId: String): Boolean =
        _accessibleModules.value.contains(moduleId)

    override fun updateContext(context: UserContext?) {
        _userContext.value = context
        _permissions.value = context?.permissions ?: emptySet()
        _accessibleModules.value = context?.accessibleModules ?: emptySet()
    }

    override fun clear() {
        updateContext(null)
    }
}
