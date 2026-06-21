package com.krishifarms.mobile.core.database.entity

import androidx.room.ColumnInfo
import com.krishifarms.mobile.core.common.SyncStatus

/**
 * Embedded sync metadata for offline-first entities.
 * All domain tables should include this via Room @Embedded.
 */
data class SyncMetadata(
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null,
    @ColumnInfo(name = "local_updated_at")
    val localUpdatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sync_error")
    val syncError: String? = null,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
)
