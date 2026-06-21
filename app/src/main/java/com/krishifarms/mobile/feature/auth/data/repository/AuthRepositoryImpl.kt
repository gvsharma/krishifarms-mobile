package com.krishifarms.mobile.feature.auth.data.repository

import com.krishifarms.mobile.core.common.Result
import com.krishifarms.mobile.core.security.session.SessionManager
import com.krishifarms.mobile.core.security.session.SessionSource
import com.krishifarms.mobile.core.network.NetworkResult
import com.krishifarms.mobile.core.network.safeApiCall
import com.krishifarms.mobile.feature.auth.data.dto.LoginRequest
import com.krishifarms.mobile.feature.auth.data.dto.RefreshTokenRequest
import com.krishifarms.mobile.feature.auth.data.local.AuthPreferences
import com.krishifarms.mobile.feature.auth.data.local.TokenStorage
import com.krishifarms.mobile.feature.auth.data.mapper.SessionMapper
import com.krishifarms.mobile.feature.auth.data.remote.AuthApi
import com.krishifarms.mobile.feature.auth.domain.model.User
import com.krishifarms.mobile.feature.auth.domain.model.UserSession
import com.krishifarms.mobile.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
    private val authPreferences: AuthPreferences,
    private val sessionManager: SessionManager,
    private val sessionMapper: SessionMapper,
) : AuthRepository {

    override val currentUser: Flow<User?> = sessionManager.session.map { it?.user }

    override val observeSession: Flow<UserSession?> = sessionManager.session.map { context ->
        context?.let {
            UserSession(
                user = it.user,
                roles = it.roles,
                permissions = it.permissions,
                accessibleModules = it.accessibleModules,
                loadedAtMillis = it.loadedAtMillis,
                source = it.source,
            )
        }
    }

    override val isLoggedIn: Flow<Boolean> = sessionManager.session.map { it != null }

    override suspend fun login(
        mobile: String,
        password: String,
        rememberLogin: Boolean,
    ): Result<UserSession> {
        return when (val result = safeApiCall {
            authApi.login(LoginRequest(mobile = mobile, password = password))
        }) {
            is NetworkResult.Success -> {
                val tokens = result.data.data
                tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
                authPreferences.setRememberLogin(rememberLogin)
                sessionManager.updateFromLogin(tokens, SessionSource.LOGIN)
                val session = sessionMapper.fromTokenResponse(tokens, SessionSource.LOGIN)
                authPreferences.saveUser(session.user)
                Result.Success(session)
            }

            is NetworkResult.Error -> Result.Error(result.message)
        }
    }

    override suspend fun refreshToken(): Result<Unit> {
        val refreshToken = tokenStorage.getRefreshToken()
            ?: return Result.Error("No refresh token available")

        return when (val result = safeApiCall {
            authApi.refreshToken(RefreshTokenRequest(refreshToken))
        }) {
            is NetworkResult.Success -> {
                val tokens = result.data.data
                tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
                sessionManager.updateFromLogin(tokens, SessionSource.REFRESH)
                tokens.user?.let { authPreferences.saveUser(sessionMapper.fromTokenResponse(tokens, SessionSource.REFRESH).user) }
                Result.Success(Unit)
            }

            is NetworkResult.Error -> {
                clearLocalSession(clearRememberLogin = false)
                Result.Error(result.message)
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        val refreshToken = tokenStorage.getRefreshToken()
        if (!refreshToken.isNullOrBlank()) {
            safeApiCall {
                authApi.logout(RefreshTokenRequest(refreshToken))
            }
        }
        clearLocalSession(clearRememberLogin = true)
        return Result.Success(Unit)
    }

    override suspend fun restoreSession(): Result<UserSession?> {
        val rememberLogin = authPreferences.isRememberLoginEnabled()
        if (!rememberLogin) {
            clearLocalSession(clearRememberLogin = false)
            return Result.Success(null)
        }

        if (!tokenStorage.hasValidSession()) {
            clearLocalSession(clearRememberLogin = false)
            return Result.Success(null)
        }

        sessionManager.restore()?.let { context ->
            authPreferences.saveUser(context.user)
            return Result.Success(
                UserSession(
                    user = context.user,
                    roles = context.roles,
                    permissions = context.permissions,
                    accessibleModules = context.accessibleModules,
                    loadedAtMillis = context.loadedAtMillis,
                    source = context.source,
                ),
            )
        }

        return when (refreshToken()) {
            is Result.Success -> {
                val context = sessionManager.session.value
                Result.Success(
                    context?.let {
                        UserSession(
                            user = it.user,
                            roles = it.roles,
                            permissions = it.permissions,
                            accessibleModules = it.accessibleModules,
                            loadedAtMillis = it.loadedAtMillis,
                            source = it.source,
                        )
                    },
                )
            }
            is Result.Error -> Result.Success(null)
            is Result.Loading -> Result.Success(null)
        }
    }

    private suspend fun clearLocalSession(clearRememberLogin: Boolean) {
        tokenStorage.clearTokens()
        authPreferences.clearSessionPreferences()
        sessionManager.clear()
        if (clearRememberLogin) {
            authPreferences.setRememberLogin(false)
        }
    }
}
