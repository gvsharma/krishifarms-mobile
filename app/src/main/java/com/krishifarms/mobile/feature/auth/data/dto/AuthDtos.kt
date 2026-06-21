package com.krishifarms.mobile.feature.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val mobile: String,
    val password: String,
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
    val user: UserDto? = null,
    val roles: List<String> = emptyList(),
    val permissions: List<String> = emptyList(),
    @SerialName("accessibleModules") val accessibleModules: List<String> = emptyList(),
)

@Serializable
data class LoginResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
    val user: UserDto? = null,
    val roles: List<String> = emptyList(),
    val permissions: List<String> = emptyList(),
    @SerialName("accessibleModules") val accessibleModules: List<String> = emptyList(),
)

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val mobile: String,
    val email: String? = null,
    val role: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
)
