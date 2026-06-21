package com.krishifarms.mobile.core.domain.repository

import com.krishifarms.mobile.core.common.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class SyncState(
    val pendingCount: Int = 0,
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val lastError: String? = null,
)

interface SyncRepository {
    fun observeSyncState(): Flow<SyncState>
    suspend fun triggerSync(): Result<Unit>
}
