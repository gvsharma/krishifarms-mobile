package com.krishifarms.mobile.feature.auth.domain.model

data class User(
    val id: String,
    val name: String,
    val mobile: String,
    val email: String?,
    val role: String?,
)
