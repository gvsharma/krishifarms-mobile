package com.krishifarms.mobile.feature.auth.domain.repository

import com.krishifarms.mobile.core.common.Result
import com.krishifarms.mobile.feature.auth.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    val isLoggedIn: Flow<Boolean>

    suspend fun login(mobile: String, password: String, rememberLogin: Boolean): Result<User>
    suspend fun refreshToken(): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun restoreSession(): Result<User?>
}
