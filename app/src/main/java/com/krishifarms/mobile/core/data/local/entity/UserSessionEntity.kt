package com.krishifarms.mobile.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_sessions")
data class UserSessionEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val mobile: String,
    val email: String?,
    val role: String?,
    val rolesJson: String? = null,
    val permissionsJson: String? = null,
    val accessibleModulesJson: String? = null,
    val lastLoginAt: Long,
)
