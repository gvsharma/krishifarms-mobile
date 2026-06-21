package com.krishifarms.mobile.feature.auth.data.remote

import com.krishifarms.mobile.core.network.ApiResponse
import com.krishifarms.mobile.feature.auth.data.dto.LoginRequest
import com.krishifarms.mobile.feature.auth.data.dto.RefreshTokenRequest
import com.krishifarms.mobile.feature.auth.data.dto.TokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): ApiResponse<TokenResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest,
    ): ApiResponse<TokenResponse>

    @POST("auth/logout")
    suspend fun logout(
        @Body request: RefreshTokenRequest,
    ): ApiResponse<Map<String, String>>
}
