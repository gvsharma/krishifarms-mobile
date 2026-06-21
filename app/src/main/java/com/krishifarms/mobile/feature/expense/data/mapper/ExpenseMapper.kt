package com.krishifarms.mobile.feature.expense.data.mapper

import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.database.entity.ExpenseEntity
import com.krishifarms.mobile.core.database.entity.SyncMetadata
import com.krishifarms.mobile.feature.expense.data.remote.dto.ExpenseDto
import com.krishifarms.mobile.feature.expense.domain.model.Expense
import com.krishifarms.mobile.feature.expense.domain.model.ExpenseCategory
import com.krishifarms.mobile.feature.expense.domain.model.PaymentMethod

fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    serverId = serverId,
    category = category,
    amount = amount,
    description = description,
    expenseDate = expenseDate,
    vendor = vendor,
    paymentMethod = paymentMethod,
    localBillPath = localBillPath,
    remoteBillUrl = remoteBillUrl,
    syncStatus = sync.syncStatus,
    createdAt = createdAt,
)

fun ExpenseDto.toEntity(existing: ExpenseEntity? = null): ExpenseEntity = ExpenseEntity(
    id = existing?.id ?: id,
    serverId = id,
    category = ExpenseCategory.valueOf(category),
    amount = amount,
    description = description,
    expenseDate = expenseDate,
    vendor = vendor,
    paymentMethod = paymentMethod?.let { runCatching { PaymentMethod.valueOf(it) }.getOrNull() },
    localBillPath = existing?.localBillPath,
    remoteBillUrl = billUrl ?: existing?.remoteBillUrl,
    createdAt = createdAt,
    sync = SyncMetadata(
        syncStatus = SyncStatus.SYNCED,
        lastSyncedAt = System.currentTimeMillis(),
        localUpdatedAt = existing?.sync?.localUpdatedAt ?: updatedAt,
    ),
)
