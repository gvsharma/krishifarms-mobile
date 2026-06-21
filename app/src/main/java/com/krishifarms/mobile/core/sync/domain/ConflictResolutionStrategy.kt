package com.krishifarms.mobile.core.sync.domain

enum class ConflictResolutionStrategy {
    SERVER_WINS,
    CLIENT_WINS,
    MERGE,
    MANUAL,
}
