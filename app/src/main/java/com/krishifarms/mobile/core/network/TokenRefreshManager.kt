package com.krishifarms.mobile.core.network

import com.krishifarms.mobile.feature.auth.data.local.TokenStorage
import com.krishifarms.mobile.feature.auth.data.remote.AuthApi
import com.krishifarms.mobile.feature.auth.data.dto.RefreshTokenRequest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

interface TokenRefresher {
    suspend fun refreshAccessToken(): Boolean
}

@Singleton
class TokenRefreshManager @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
) : TokenRefresher {

    private val refreshMutex = Mutex()

    override suspend fun refreshAccessToken(): Boolean = refreshMutex.withLock {
        val refreshToken = tokenStorage.getRefreshToken() ?: return false

        return runCatching {
            val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))
            val tokens = response.data
            tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
            true
        }.getOrDefault(false)
    }
}
