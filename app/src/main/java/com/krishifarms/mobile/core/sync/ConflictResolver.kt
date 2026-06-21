package com.krishifarms.mobile.core.sync

import com.krishifarms.mobile.core.database.dao.SyncConflictDao
import com.krishifarms.mobile.core.database.entity.SyncConflictEntity
import com.krishifarms.mobile.core.database.entity.SyncOperationEntity
import com.krishifarms.mobile.core.sync.domain.ConflictResolutionStrategy
import com.krishifarms.mobile.core.sync.domain.RemoteEntitySnapshot
import com.krishifarms.mobile.core.sync.domain.SyncHandler
import com.krishifarms.mobile.core.sync.domain.SyncHandlerResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConflictResolver @Inject constructor(
    private val syncConflictDao: SyncConflictDao,
    private val syncLogger: SyncLogger,
) {
    suspend fun resolve(
        operation: SyncOperationEntity,
        serverSnapshot: RemoteEntitySnapshot,
        clientUpdatedAt: Long,
        handler: SyncHandler,
        strategy: ConflictResolutionStrategy = ConflictResolutionStrategy.SERVER_WINS,
    ): SyncHandlerResult {
        val effectiveStrategy = when (strategy) {
            ConflictResolutionStrategy.SERVER_WINS -> {
                if (serverSnapshot.updatedAt >= clientUpdatedAt) {
                    ConflictResolutionStrategy.SERVER_WINS
                } else {
                    ConflictResolutionStrategy.CLIENT_WINS
                }
            }
            else -> strategy
        }

        return when (effectiveStrategy) {
            ConflictResolutionStrategy.SERVER_WINS -> {
                syncLogger.d("Conflict resolved: SERVER_WINS for ${operation.entityType}/${operation.entityId}")
                SyncHandlerResult.Success(serverEntityId = serverSnapshot.entityId)
            }

            ConflictResolutionStrategy.CLIENT_WINS -> {
                syncLogger.d("Conflict resolved: CLIENT_WINS for ${operation.entityType}/${operation.entityId}")
                handler.execute(operation)
            }

            ConflictResolutionStrategy.MERGE,
            ConflictResolutionStrategy.MANUAL,
            -> {
                syncConflictDao.insert(
                    SyncConflictEntity(
                        operationId = operation.id,
                        entityType = operation.entityType,
                        entityId = operation.entityId,
                        clientPayloadJson = operation.payloadJson,
                        serverPayloadJson = serverSnapshot.payloadJson,
                        clientUpdatedAt = clientUpdatedAt,
                        serverUpdatedAt = serverSnapshot.updatedAt,
                    ),
                )
                syncLogger.w("Conflict stored for manual review: ${operation.entityType}/${operation.entityId}")
                SyncHandlerResult.PermanentFailure("Conflict requires manual resolution")
            }
        }
    }
}
