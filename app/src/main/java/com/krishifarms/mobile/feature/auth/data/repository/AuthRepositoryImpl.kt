package com.krishifarms.mobile.feature.auth.data.repository

import com.krishifarms.mobile.core.common.Result
import com.krishifarms.mobile.core.database.dao.UserSessionDao
import com.krishifarms.mobile.core.data.local.entity.UserSessionEntity
import com.krishifarms.mobile.core.network.NetworkResult
import com.krishifarms.mobile.core.network.safeApiCall
import com.krishifarms.mobile.feature.auth.data.dto.LoginRequest
import com.krishifarms.mobile.feature.auth.data.dto.RefreshTokenRequest
import com.krishifarms.mobile.feature.auth.data.dto.UserDto
import com.krishifarms.mobile.feature.auth.data.local.AuthPreferences
import com.krishifarms.mobile.feature.auth.data.local.TokenStorage
import com.krishifarms.mobile.feature.auth.data.remote.AuthApi
import com.krishifarms.mobile.feature.auth.domain.model.User
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
    private val userSessionDao: UserSessionDao,
) : AuthRepository {

    override val currentUser: Flow<User?> = authPreferences.cachedUser

    override val isLoggedIn: Flow<Boolean> = currentUser.map { user ->
        user != null && tokenStorage.hasValidSession()
    }

    override suspend fun login(
        mobile: String,
        password: String,
        rememberLogin: Boolean,
    ): Result<User> {
        return when (val result = safeApiCall {
            authApi.login(LoginRequest(mobile = mobile, password = password))
        }) {
            is NetworkResult.Success -> {
                val tokens = result.data.data
                tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
                authPreferences.setRememberLogin(rememberLogin)

                val user = tokens.user?.toDomain()
                    ?: User(
                        id = mobile,
                        name = mobile,
                        mobile = mobile,
                        email = null,
                        role = null,
                    )

                authPreferences.saveUser(user)
                persistUserSession(user)
                Result.Success(user)
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
                tokens.user?.toDomain()?.let { persistUserSession(it) }
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

    override suspend fun restoreSession(): Result<User?> {
        val rememberLogin = authPreferences.isRememberLoginEnabled()
        if (!rememberLogin) {
            clearLocalSession(clearRememberLogin = false)
            return Result.Success(null)
        }

        if (!tokenStorage.hasValidSession()) {
            clearLocalSession(clearRememberLogin = false)
            return Result.Success(null)
        }

        authPreferences.getCachedUser()?.let { return Result.Success(it) }

        userSessionDao.getSession()?.toDomain()?.let { return Result.Success(it) }

        return when (refreshToken()) {
            is Result.Success -> Result.Success(authPreferences.getCachedUser())
            is Result.Error -> Result.Success(null)
            is Result.Loading -> Result.Success(null)
        }
    }

    private suspend fun clearLocalSession(clearRememberLogin: Boolean) {
        tokenStorage.clearTokens()
        authPreferences.clearSessionPreferences()
        userSessionDao.clear()
        if (clearRememberLogin) {
            authPreferences.setRememberLogin(false)
        }
    }

    private suspend fun persistUserSession(user: User) {
        authPreferences.saveUser(user)
        userSessionDao.upsert(
            UserSessionEntity(
                userId = user.id,
                name = user.name,
                mobile = user.mobile,
                email = user.email,
                role = user.role,
                lastLoginAt = System.currentTimeMillis(),
            ),
        )
    }

    private fun UserSessionEntity.toDomain(): User = User(
        id = userId,
        name = name,
        mobile = mobile,
        email = email,
        role = role,
    )

    private fun UserDto.toDomain(): User = User(
        id = id,
        name = name,
        mobile = mobile,
        email = email,
        role = role,
    )
}
