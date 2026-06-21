package com.krishifarms.mobile.feature.expense.data.repository

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.krishifarms.mobile.core.common.DispatcherProvider
import com.krishifarms.mobile.core.common.IdGenerator
import com.krishifarms.mobile.core.common.NetworkMonitor
import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.dao.ExpenseDao
import com.krishifarms.mobile.core.database.entity.ExpenseEntity
import com.krishifarms.mobile.core.database.entity.SyncMetadata
import com.krishifarms.mobile.core.network.NetworkResult
import com.krishifarms.mobile.core.network.safeApiCall
import com.krishifarms.mobile.core.sync.OfflineSyncEngine
import com.krishifarms.mobile.core.sync.domain.SyncEntityType
import com.krishifarms.mobile.core.sync.domain.SyncOperationType
import com.krishifarms.mobile.core.sync.handler.ExpenseSyncPayload
import com.krishifarms.mobile.feature.expense.data.mapper.toDomain
import com.krishifarms.mobile.feature.expense.data.mapper.toEntity
import com.krishifarms.mobile.feature.expense.data.remote.ExpenseApi
import com.krishifarms.mobile.feature.expense.domain.model.Expense
import com.krishifarms.mobile.feature.expense.domain.model.ExpenseCategory
import com.krishifarms.mobile.feature.expense.domain.repository.CreateExpenseInput
import com.krishifarms.mobile.feature.expense.domain.repository.ExpenseRepository
import com.krishifarms.mobile.feature.expense.sync.ExpenseSyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val expenseApi: ExpenseApi,
    private val networkMonitor: NetworkMonitor,
    private val dispatchers: DispatcherProvider,
    private val offlineSyncEngine: OfflineSyncEngine,
    private val workManager: WorkManager,
    private val json: Json,
) : ExpenseRepository {

    override fun observeExpenses(
        searchQuery: String,
        category: ExpenseCategory?,
        startDate: Long?,
        endDate: Long?,
    ): Flow<List<Expense>> = expenseDao.observeFiltered(
        searchQuery = searchQuery.trim(),
        category = category,
        startDate = startDate,
        endDate = endDate,
    ).map { entities -> entities.map { it.toDomain() } }

    override fun observeExpense(id: String): Flow<Expense?> =
        expenseDao.observeById(id).map { it?.toDomain() }

    override fun observePendingCount(): Flow<Int> = expenseDao.observePendingCount()

    override suspend fun createExpense(input: CreateExpenseInput): Result<String> =
        withContext(dispatchers.io) {
            runCatching {
                val localId = IdGenerator.newLocalId()
                val now = System.currentTimeMillis()
                val entity = ExpenseEntity(
                    id = localId,
                    serverId = null,
                    category = input.category,
                    amount = input.amount,
                    description = input.description.trim(),
                    expenseDate = input.expenseDate,
                    vendor = input.vendor?.trim()?.takeIf { it.isNotBlank() },
                    paymentMethod = input.paymentMethod,
                    localBillPath = input.localBillPath,
                    remoteBillUrl = null,
                    createdAt = now,
                    sync = SyncMetadata(
                        syncStatus = SyncStatus.PENDING_CREATE,
                        localUpdatedAt = now,
                    ),
                )
                expenseDao.upsert(entity)

                val payload = ExpenseSyncPayload(
                    category = input.category.name,
                    description = input.description.trim(),
                    amount = input.amount,
                    expenseDate = input.expenseDate,
                    vendor = input.vendor,
                    paymentMethod = input.paymentMethod?.name,
                    localUpdatedAt = now,
                )

                offlineSyncEngine.enqueue(
                    entityType = SyncEntityType.EXPENSE,
                    entityId = localId,
                    operationType = SyncOperationType.CREATE,
                    payloadJson = json.encodeToString(ExpenseSyncPayload.serializer(), payload),
                    idempotencyKey = localId,
                )
                scheduleSync()
                localId
            }
        }

    override suspend fun refreshFromServer() = withContext(dispatchers.io) {
        if (!networkMonitor.isOnline()) return@withContext
        var page = 1
        var totalPages = 1
        while (page <= totalPages) {
            when (val result = safeApiCall { expenseApi.getExpenses(page = page, pageSize = PAGE_SIZE) }) {
                is NetworkResult.Success -> {
                    val payload = result.data.data
                    totalPages = payload.totalPages
                    payload.items.forEach { dto ->
                        val existing = expenseDao.getById(dto.id)
                            ?: expenseDao.getById("local_${dto.id}")
                        expenseDao.upsert(dto.toEntity(existing))
                    }
                    page++
                }
                is NetworkResult.Error -> break
            }
        }
    }

    override suspend fun syncPending() {
        withContext(dispatchers.io) {
            offlineSyncEngine.processQueue()
        }
    }

    private fun scheduleSync() {
        val request = OneTimeWorkRequestBuilder<ExpenseSyncWorker>().build()
        workManager.enqueueUniqueWork(
            ExpenseSyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
