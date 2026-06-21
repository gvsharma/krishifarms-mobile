package com.krishifarms.mobile.core.common

enum class SyncStatus {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE,
    SYNC_FAILED,
}

fun SyncStatus.isPending(): Boolean = when (this) {
    SyncStatus.SYNCED -> false
    SyncStatus.PENDING_CREATE,
    SyncStatus.PENDING_UPDATE,
    SyncStatus.PENDING_DELETE,
    SyncStatus.SYNC_FAILED,
    -> true
}
