package com.krishifarms.mobile.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T,
    val meta: Meta? = null,
)

@Serializable
data class Meta(
    @SerialName("request_id") val requestId: String? = null,
)

@Serializable
data class ApiErrorResponse(
    val success: Boolean = false,
    val error: ApiError? = null,
)

@Serializable
data class ApiError(
    val message: String,
    val details: Map<String, String>? = null,
)
