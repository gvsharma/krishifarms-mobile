package com.krishifarms.mobile.core.sync

import com.krishifarms.mobile.core.common.DispatcherProvider
import com.krishifarms.mobile.core.common.NetworkMonitor
import com.krishifarms.mobile.core.database.dao.SyncMetadataDao
import com.krishifarms.mobile.core.database.dao.SyncQueueDao
import com.krishifarms.mobile.core.database.entity.SyncMetadataEntity
import com.krishifarms.mobile.core.database.entity.SyncOperationEntity
import com.krishifarms.mobile.core.sync.domain.ConflictResolutionStrategy
import com.krishifarms.mobile.core.sync.domain.OperationStatus
import com.krishifarms.mobile.core.sync.domain.SyncEntityType
import com.krishifarms.mobile.core.sync.domain.SyncHandler
import com.krishifarms.mobile.core.sync.domain.SyncHandlerResult
import com.krishifarms.mobile.core.sync.domain.SyncOperationType
import com.krishifarms.mobile.core.sync.worker.SyncScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import javax.inject.Inject
import javax.inject.Singleton

interface OfflineSyncEngine {
    suspend fun enqueue(
        entityType: SyncEntityType,
        entityId: String,
        operationType: SyncOperationType,
        payloadJson: String,
        idempotencyKey: String,
        priority: Int = 0,
    ): Long?

    suspend fun processQueue(): SyncProcessResult

    fun observePendingCount(): Flow<Int>

    fun observeUnresolvedOperations(): Flow<List<SyncOperationEntity>>
}

data class SyncProcessResult(
    val processed: Int,
    val succeeded: Int,
    val failed: Int,
    val skipped: Int,
)

@Singleton
class SyncEngine @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val syncMetadataDao: SyncMetadataDao,
    private val syncScheduler: SyncScheduler,
    private val networkMonitor: NetworkMonitor,
    private val conflictResolver: ConflictResolver,
    private val syncHandlers: Map<SyncEntityType, @JvmSuppressWildcards SyncHandler>,
    private val dispatcherProvider: DispatcherProvider,
    private val syncLogger: SyncLogger,
    private val json: Json,
) : OfflineSyncEngine {

    override suspend fun enqueue(
        entityType: SyncEntityType,
        entityId: String,
        operationType: SyncOperationType,
        payloadJson: String,
        idempotencyKey: String,
        priority: Int,
    ): Long? = withContext(dispatcherProvider.io) {
        val existing = syncQueueDao.getByIdempotencyKey(idempotencyKey)
        if (existing != null && existing.status != OperationStatus.FAILED) {
            syncLogger.d("Skipping duplicate enqueue for idempotency key=$idempotencyKey status=${existing.status}")
            return@withContext existing.id
        }

        val operation = SyncOperationEntity(
            entityType = entityType,
            entityId = entityId,
            operationType = operationType,
            payloadJson = payloadJson,
            idempotencyKey = idempotencyKey,
            priority = priority,
        )

        val id = runCatching { syncQueueDao.insert(operation) }
            .onFailure { syncLogger.e("Failed to enqueue operation", it) }
            .getOrNull()

        if (id != null) {
            syncLogger.d("Enqueued ${operationType.name} ${entityType.name}/$entityId id=$id")
            syncScheduler.scheduleImmediateSync()
        }
        id
    }

    override suspend fun processQueue(): SyncProcessResult = withContext(dispatcherProvider.io) {
        if (!networkMonitor.isOnline()) {
            syncLogger.d("Skipping sync: device offline")
            return@withContext SyncProcessResult(processed = 0, succeeded = 0, failed = 0, skipped = 0)
        }

        val pending = syncQueueDao.getPending()
        var succeeded = 0
        var failed = 0
        var skipped = 0

        for (operation in pending) {
            val handler = syncHandlers[operation.entityType]
            if (handler == null) {
                syncQueueDao.markFailed(operation.id, "No handler registered for ${operation.entityType}")
                failed++
                continue
            }

            syncQueueDao.markInProgress(operation.id)
            when (val result = executeOperation(operation, handler)) {
                is OperationExecution.Success -> {
                    syncQueueDao.markSuccess(operation.id)
                    updateLastSyncTimestamp(operation.entityType)
                    succeeded++
                }

                is OperationExecution.Failed -> {
                    if (operation.retryCount + 1 >= operation.maxRetries) {
                        syncQueueDao.markFailed(operation.id, result.message)
                    } else {
                        syncQueueDao.incrementRetry(operation.id)
                        syncQueueDao.markFailed(operation.id, result.message)
                    }
                    failed++
                }

                is OperationExecution.ConflictStored -> {
                    syncQueueDao.markConflict(operation.id, result.message)
                    failed++
                }

                OperationExecution.Skipped -> skipped++
            }
        }

        val pruneBefore = System.currentTimeMillis() - SUCCESS_RETENTION_MS
        syncQueueDao.pruneSuccessful(pruneBefore)

        val result = SyncProcessResult(
            processed = pending.size,
            succeeded = succeeded,
            failed = failed,
            skipped = skipped,
        )
        syncLogger.d("Sync complete: $result")
        result
    }

    override fun observePendingCount(): Flow<Int> = syncQueueDao.observePendingCount()

    override fun observeUnresolvedOperations(): Flow<List<SyncOperationEntity>> =
        syncQueueDao.observeUnresolved()

    private suspend fun executeOperation(
        operation: SyncOperationEntity,
        handler: SyncHandler,
    ): OperationExecution {
        return when (val result = handler.execute(operation)) {
            is SyncHandlerResult.Success -> OperationExecution.Success

            is SyncHandlerResult.RetryableFailure -> {
                syncLogger.w("Retryable failure for op=${operation.id}: ${result.message}")
                OperationExecution.Failed(result.message)
            }

            is SyncHandlerResult.PermanentFailure -> {
                syncLogger.e("Permanent failure for op=${operation.id}: ${result.message}")
                OperationExecution.Failed(result.message)
            }

            is SyncHandlerResult.Conflict -> {
                val resolution = conflictResolver.resolve(
                    operation = operation,
                    serverSnapshot = result.serverSnapshot,
                    clientUpdatedAt = result.clientUpdatedAt,
                    handler = handler,
                    strategy = ConflictResolutionStrategy.SERVER_WINS,
                )
                when (resolution) {
                    is SyncHandlerResult.Success -> OperationExecution.Success
                    is SyncHandlerResult.RetryableFailure -> OperationExecution.Failed(resolution.message)
                    is SyncHandlerResult.PermanentFailure -> OperationExecution.ConflictStored(resolution.message)
                    is SyncHandlerResult.Conflict -> OperationExecution.ConflictStored("Unresolved conflict")
                }
            }
        }
    }

    private suspend fun updateLastSyncTimestamp(entityType: SyncEntityType) {
        val now = System.currentTimeMillis()
        syncMetadataDao.upsert(
            SyncMetadataEntity(
                entityType = entityType,
                lastSyncTimestamp = now,
            ),
        )
    }

    private sealed interface OperationExecution {
        data object Success : OperationExecution
        data class Failed(val message: String) : OperationExecution
        data class ConflictStored(val message: String) : OperationExecution
        data object Skipped : OperationExecution
    }

    private companion object {
        const val SUCCESS_RETENTION_MS = 7L * 24 * 60 * 60 * 1000
    }
}

fun extractUpdatedAt(payloadJson: String, json: Json): Long {
    return runCatching {
        val element = json.parseToJsonElement(payloadJson)
        if (element is JsonObject) {
            element["updatedAt"]?.jsonPrimitive?.longOrNull
                ?: element["localUpdatedAt"]?.jsonPrimitive?.longOrNull
        } else {
            null
        }
    }.getOrNull() ?: System.currentTimeMillis()
}
