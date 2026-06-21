package com.krishifarms.mobile.core.sync.domain

import com.krishifarms.mobile.core.database.entity.SyncOperationEntity

interface SyncHandler {
    val entityType: SyncEntityType

    suspend fun execute(operation: SyncOperationEntity): SyncHandlerResult

    suspend fun fetchRemote(entityId: String): RemoteEntitySnapshot?
}

data class RemoteEntitySnapshot(
    val entityId: String,
    val updatedAt: Long,
    val payloadJson: String,
)

sealed interface SyncHandlerResult {
    data class Success(val serverEntityId: String? = null) : SyncHandlerResult
    data class RetryableFailure(val message: String) : SyncHandlerResult
    data class PermanentFailure(val message: String) : SyncHandlerResult
    data class Conflict(
        val serverSnapshot: RemoteEntitySnapshot,
        val clientUpdatedAt: Long,
    ) : SyncHandlerResult
}
