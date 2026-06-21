package com.krishifarms.mobile.core.sync.handler

import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.dao.ExpenseDao
import com.krishifarms.mobile.core.database.entity.ExpenseEntity
import com.krishifarms.mobile.core.database.entity.SyncMetadata
import com.krishifarms.mobile.core.database.entity.SyncOperationEntity
import com.krishifarms.mobile.core.network.ExpenseApiService
import com.krishifarms.mobile.core.network.dto.ExpenseDtos
import com.krishifarms.mobile.core.sync.domain.RemoteEntitySnapshot
import com.krishifarms.mobile.core.sync.domain.SyncEntityType
import com.krishifarms.mobile.core.sync.domain.SyncHandler
import com.krishifarms.mobile.core.sync.domain.SyncHandlerResult
import com.krishifarms.mobile.core.sync.domain.SyncOperationType
import com.krishifarms.mobile.feature.expense.domain.model.ExpenseCategory
import com.krishifarms.mobile.feature.expense.domain.model.PaymentMethod
import com.krishifarms.mobile.feature.expense.data.remote.ExpenseApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseSyncHandler @Inject constructor(
    private val expenseApi: ExpenseApiService,
    private val featureExpenseApi: ExpenseApi,
    private val expenseDao: ExpenseDao,
    json: Json,
) : SyncHandler, BaseSyncHandler(json) {

    override val entityType: SyncEntityType = SyncEntityType.EXPENSE

    override suspend fun execute(operation: SyncOperationEntity): SyncHandlerResult {
        val payload = json.decodeFromString<ExpenseSyncPayload>(operation.payloadJson)
        return when (operation.operationType) {
            SyncOperationType.CREATE -> executeApi {
                val response = expenseApi.createExpense(
                    idempotencyKey = operation.idempotencyKey,
                    request = ExpenseDtos.CreateExpenseRequest(
                        category = payload.category,
                        description = payload.description,
                        amount = payload.amount,
                        spentAt = payload.expenseDate,
                        vendor = payload.vendor,
                        paymentMethod = payload.paymentMethod,
                        farmId = null,
                        idempotencyKey = operation.idempotencyKey,
                    ),
                )
                val existing = expenseDao.getById(operation.entityId)
                var entity = response.toLocalEntity(operation.entityId, payload, existing)
                entity = uploadBillIfNeeded(entity, response.id)
                expenseDao.upsert(entity)
                response
            }.let { if (it is SyncHandlerResult.Success) SyncHandlerResult.Success() else it }

            SyncOperationType.UPDATE -> executeApi {
                val response = expenseApi.updateExpense(
                    id = operation.entityId,
                    idempotencyKey = operation.idempotencyKey,
                    request = ExpenseDtos.UpdateExpenseRequest(
                        category = payload.category,
                        description = payload.description,
                        amount = payload.amount,
                        spentAt = payload.expenseDate,
                    ),
                )
                expenseDao.upsert(response.toLocalEntity(operation.entityId, payload, expenseDao.getById(operation.entityId)))
                response
            }

            SyncOperationType.DELETE -> {
                expenseDao.update(
                    expenseDao.getById(operation.entityId)?.copy(
                        sync = SyncMetadata(syncStatus = SyncStatus.SYNCED),
                    ) ?: return SyncHandlerResult.Success(),
                )
                SyncHandlerResult.Success()
            }
        }
    }

    override suspend fun fetchRemote(entityId: String): RemoteEntitySnapshot? {
        return runCatching {
            val dto = expenseApi.getExpense(entityId)
            RemoteEntitySnapshot(
                entityId = dto.id,
                updatedAt = dto.updatedAt,
                payloadJson = json.encodeToString(ExpenseDtos.ExpenseDto.serializer(), dto),
            )
        }.getOrNull()
    }

    private fun ExpenseDtos.ExpenseDto.toLocalEntity(
        localId: String,
        payload: ExpenseSyncPayload,
        existing: ExpenseEntity?,
    ): ExpenseEntity = ExpenseEntity(
        id = localId,
        serverId = id,
        category = runCatching { ExpenseCategory.valueOf(payload.category) }.getOrDefault(ExpenseCategory.MISCELLANEOUS),
        amount = amount,
        description = description,
        expenseDate = spentAt,
        vendor = payload.vendor,
        paymentMethod = payload.paymentMethod?.let {
            runCatching { PaymentMethod.valueOf(it) }.getOrNull()
        },
        localBillPath = existing?.localBillPath,
        remoteBillUrl = billUrl,
        createdAt = createdAt,
        sync = SyncMetadata(
            syncStatus = SyncStatus.SYNCED,
            lastSyncedAt = System.currentTimeMillis(),
            localUpdatedAt = updatedAt,
        ),
    )

    private suspend fun uploadBillIfNeeded(entity: ExpenseEntity, serverId: String): ExpenseEntity {
        val path = entity.localBillPath ?: return entity
        val file = File(path)
        if (!file.exists()) return entity

        val part = MultipartBody.Part.createFormData(
            "bill",
            file.name,
            file.asRequestBody("application/octet-stream".toMediaTypeOrNull()),
        )
        return runCatching {
            val upload = featureExpenseApi.uploadBill(serverId, part)
            entity.copy(remoteBillUrl = upload.data.url)
        }.getOrDefault(entity)
    }
}
