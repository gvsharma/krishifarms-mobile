package com.krishifarms.mobile.core.sync

import com.krishifarms.mobile.core.sync.network.ConnectivityObserver
import com.krishifarms.mobile.core.sync.worker.SyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncReconnectCoordinator @Inject constructor(
    private val connectivityObserver: ConnectivityObserver,
    private val syncScheduler: SyncScheduler,
    private val syncLogger: SyncLogger,
) {
    fun start(scope: CoroutineScope) {
        scope.launch {
            connectivityObserver.isConnected
                .distinctUntilChanged()
                .drop(1)
                .collect { connected ->
                    if (connected) {
                        syncLogger.d("Network restored — scheduling immediate sync")
                        syncScheduler.scheduleImmediateSync()
                    }
                }
        }
        syncScheduler.schedulePeriodicSync()
    }
}
