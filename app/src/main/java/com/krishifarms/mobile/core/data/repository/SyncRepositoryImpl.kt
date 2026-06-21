package com.krishifarms.mobile.core.data.repository

import com.krishifarms.mobile.core.common.NetworkMonitor
import com.krishifarms.mobile.core.common.Result
import com.krishifarms.mobile.core.domain.repository.SyncRepository
import com.krishifarms.mobile.core.domain.repository.SyncState
import com.krishifarms.mobile.core.sync.OfflineSyncEngine
import com.krishifarms.mobile.core.sync.worker.SyncScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val offlineSyncEngine: OfflineSyncEngine,
    private val networkMonitor: NetworkMonitor,
    private val syncScheduler: SyncScheduler,
) : SyncRepository {

    override fun observeSyncState(): Flow<SyncState> = combine(
        offlineSyncEngine.observePendingCount(),
        flow { emit(networkMonitor.isOnline()) },
    ) { pendingCount, isOnline ->
        SyncState(
            pendingCount = pendingCount,
            isOnline = isOnline,
            isSyncing = false,
        )
    }

    override suspend fun triggerSync(): Result<Unit> {
        if (!networkMonitor.isOnline()) {
            return Result.Error("Cannot sync while offline")
        }
        syncScheduler.scheduleImmediateSync()
        offlineSyncEngine.processQueue()
        return Result.Success(Unit)
    }
}
