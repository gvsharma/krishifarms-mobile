package com.krishifarms.mobile.core.sync

import com.krishifarms.mobile.core.sync.domain.SyncEntityType
import com.krishifarms.mobile.core.sync.domain.SyncHandler
import com.krishifarms.mobile.core.sync.domain.SyncOperationType
import com.krishifarms.mobile.core.sync.handler.DocumentSyncHandler
import com.krishifarms.mobile.core.sync.handler.ExpenseSyncHandler
import com.krishifarms.mobile.core.sync.handler.FarmerSyncHandler
import com.krishifarms.mobile.core.sync.handler.ProcurementSyncHandler
import com.krishifarms.mobile.core.sync.handler.WorkerSyncHandler
import com.krishifarms.mobile.core.sync.network.ConnectivityObserver
import com.krishifarms.mobile.core.sync.network.DefaultConnectivityObserver
import com.krishifarms.mobile.core.sync.worker.SyncScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncBindingsModule {

    @Binds
    @Singleton
    abstract fun bindOfflineSyncEngine(impl: SyncEngine): OfflineSyncEngine

    @Binds
    @Singleton
    abstract fun bindSyncLogger(impl: AndroidSyncLogger): SyncLogger

    @Binds
    @Singleton
    abstract fun bindConnectivityObserver(impl: DefaultConnectivityObserver): ConnectivityObserver

    @Binds
    @IntoMap
    @StringKey("FARMER")
    abstract fun bindFarmerSyncHandler(handler: FarmerSyncHandler): SyncHandler

    @Binds
    @IntoMap
    @StringKey("EXPENSE")
    abstract fun bindExpenseSyncHandler(handler: ExpenseSyncHandler): SyncHandler

    @Binds
    @IntoMap
    @StringKey("PROCUREMENT")
    abstract fun bindProcurementSyncHandler(handler: ProcurementSyncHandler): SyncHandler

    @Binds
    @IntoMap
    @StringKey("WORKER")
    abstract fun bindWorkerSyncHandler(handler: WorkerSyncHandler): SyncHandler

    @Binds
    @IntoMap
    @StringKey("DOCUMENT")
    abstract fun bindDocumentSyncHandler(handler: DocumentSyncHandler): SyncHandler
}

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideSyncHandlerMap(
        handlers: Map<String, @JvmSuppressWildcards SyncHandler>,
    ): Map<SyncEntityType, SyncHandler> = handlers.mapKeys { (key, _) ->
        SyncEntityType.valueOf(key)
    }
}
