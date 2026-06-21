package com.krishifarms.mobile.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.core.data.local.entity.UserSessionEntity
import com.krishifarms.mobile.core.database.entity.AttendanceEntity
import com.krishifarms.mobile.core.database.entity.ExpenseEntity
import com.krishifarms.mobile.feature.expense.domain.model.ExpenseCategory
import com.krishifarms.mobile.core.database.entity.FarmerEntity
import com.krishifarms.mobile.core.database.entity.FarmEntity
import com.krishifarms.mobile.core.database.entity.PaymentEntity
import com.krishifarms.mobile.core.database.entity.ProcurementEntity
import com.krishifarms.mobile.core.database.entity.ProcurementWithFarmer
import com.krishifarms.mobile.core.database.entity.SyncConflictEntity
import com.krishifarms.mobile.core.database.entity.SyncMetadataEntity
import com.krishifarms.mobile.core.database.entity.SyncOperationEntity
import com.krishifarms.mobile.core.database.entity.WorkOrderEntity
import com.krishifarms.mobile.core.database.entity.WorkerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSessionDao {
    @Query("SELECT * FROM user_sessions LIMIT 1")
    suspend fun getSession(): UserSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: UserSessionEntity)

    @Query("DELETE FROM user_sessions")
    suspend fun clear()
}

@Dao
interface FarmerDao {
    @Query("SELECT * FROM farmers WHERE is_deleted = 0 ORDER BY name ASC")
    fun observeAll(): Flow<List<FarmerEntity>>

    @Query(
        """
        SELECT * FROM farmers
        WHERE is_deleted = 0
        AND (name LIKE '%' || :query || '%' OR village LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%')
        ORDER BY name ASC
        """,
    )
    fun search(query: String): Flow<List<FarmerEntity>>

    @Query("SELECT * FROM farmers WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun getById(id: String): FarmerEntity?

    @Query("SELECT * FROM farmers WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun observeById(id: String): Flow<FarmerEntity?>

    @Query("SELECT * FROM farmers WHERE sync_status != 'SYNCED' AND is_deleted = 0")
    suspend fun getPendingSync(): List<FarmerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(farmer: FarmerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(farmers: List<FarmerEntity>)

    @Update
    suspend fun update(farmer: FarmerEntity)

    @Query(
        "UPDATE farmers SET is_deleted = 1, sync_status = :status, local_updated_at = :updatedAt WHERE id = :id",
    )
    suspend fun softDelete(id: String, status: SyncStatus, updatedAt: Long = System.currentTimeMillis())
}

@Dao
interface FarmDao {
    @Query("SELECT * FROM farms WHERE is_deleted = 0 ORDER BY name ASC")
    fun observeAll(): Flow<List<FarmEntity>>

    @Query("SELECT * FROM farms WHERE farmerId = :farmerId AND is_deleted = 0")
    fun observeByFarmer(farmerId: String): Flow<List<FarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(farms: List<FarmEntity>)

    @Query("SELECT * FROM farms WHERE sync_status != 'SYNCED' AND is_deleted = 0")
    suspend fun getPendingSync(): List<FarmEntity>
}

@Dao
interface ProcurementDao {
    @Query(
        """
        SELECT p.*, f.name AS farmer_name, f.village AS farmer_village
        FROM procurements p
        INNER JOIN farmers f ON p.farmer_id = f.id
        WHERE p.is_deleted = 0
        ORDER BY p.created_at DESC
        """,
    )
    fun observeAllWithFarmer(): Flow<List<ProcurementWithFarmer>>

    @Query("SELECT * FROM procurements WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun observeById(id: String): Flow<ProcurementEntity?>

    @Query("SELECT * FROM procurements WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun getById(id: String): ProcurementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(procurement: ProcurementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(procurements: List<ProcurementEntity>)

    @Update
    suspend fun update(procurement: ProcurementEntity)

    @Query("SELECT * FROM procurements WHERE sync_status != 'SYNCED' AND is_deleted = 0")
    suspend fun getPendingSync(): List<ProcurementEntity>

    @Query("SELECT COUNT(*) FROM procurements WHERE sync_status != 'SYNCED' AND is_deleted = 0")
    fun observePendingCount(): Flow<Int>
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE is_deleted = 0 ORDER BY paidAt DESC")
    fun observeAll(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(payments: List<PaymentEntity>)

    @Query("SELECT * FROM payments WHERE sync_status != 'SYNCED' AND is_deleted = 0")
    suspend fun getPendingSync(): List<PaymentEntity>
}

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers WHERE is_deleted = 0 ORDER BY name ASC")
    fun observeAll(): Flow<List<WorkerEntity>>

    @Query(
        """
        SELECT * FROM workers
        WHERE is_deleted = 0
        AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%')
        ORDER BY name ASC
        """,
    )
    fun search(query: String): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun getById(id: String): WorkerEntity?

    @Query("SELECT * FROM workers WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun observeById(id: String): Flow<WorkerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(worker: WorkerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(workers: List<WorkerEntity>)

    @Update
    suspend fun update(worker: WorkerEntity)

    @Query("SELECT * FROM workers WHERE sync_status != 'SYNCED' AND is_deleted = 0")
    suspend fun getPendingSync(): List<WorkerEntity>

    @Query(
        "UPDATE workers SET is_deleted = 1, sync_status = :status, " +
            "local_updated_at = :updatedAt WHERE id = :id",
    )
    suspend fun softDelete(id: String, status: SyncStatus, updatedAt: Long = System.currentTimeMillis())
}

@Dao
interface WorkOrderDao {
    @Query("SELECT * FROM work_orders WHERE is_deleted = 0 ORDER BY start_time DESC")
    fun observeAll(): Flow<List<WorkOrderEntity>>

    @Query("SELECT * FROM work_orders WHERE worker_id = :workerId AND is_deleted = 0 ORDER BY start_time DESC")
    fun observeByWorker(workerId: String): Flow<List<WorkOrderEntity>>

    @Query(
        """
        SELECT * FROM work_orders
        WHERE is_deleted = 0
        AND (:workerId IS NULL OR worker_id = :workerId)
        AND (:activityType IS NULL OR activity_type = :activityType)
        AND (:farmId IS NULL OR farm_id = :farmId)
        ORDER BY start_time DESC
        """,
    )
    fun observeFiltered(
        workerId: String?,
        activityType: String?,
        farmId: String?,
    ): Flow<List<WorkOrderEntity>>

    @Query("SELECT * FROM work_orders WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun getById(id: String): WorkOrderEntity?

    @Query("SELECT * FROM work_orders WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun observeById(id: String): Flow<WorkOrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(workOrder: WorkOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(workOrders: List<WorkOrderEntity>)

    @Update
    suspend fun update(workOrder: WorkOrderEntity)

    @Query("SELECT * FROM work_orders WHERE sync_status != 'SYNCED' AND is_deleted = 0")
    suspend fun getPendingSync(): List<WorkOrderEntity>

    @Query(
        "UPDATE work_orders SET is_deleted = 1, sync_status = :status, " +
            "local_updated_at = :updatedAt WHERE id = :id",
    )
    suspend fun softDelete(id: String, status: SyncStatus, updatedAt: Long = System.currentTimeMillis())
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE is_deleted = 0 ORDER BY date DESC")
    fun observeAll(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE worker_id = :workerId AND is_deleted = 0 ORDER BY date DESC")
    fun observeByWorker(workerId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE date = :date AND is_deleted = 0 ORDER BY worker_id ASC")
    fun observeByDate(date: Long): Flow<List<AttendanceEntity>>

    @Query(
        "SELECT * FROM attendance WHERE worker_id = :workerId AND date = :date " +
            "AND is_deleted = 0 LIMIT 1",
    )
    suspend fun getByWorkerAndDate(workerId: String, date: Long): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attendance: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(records: List<AttendanceEntity>)

    @Update
    suspend fun update(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendance WHERE sync_status != 'SYNCED' AND is_deleted = 0")
    suspend fun getPendingSync(): List<AttendanceEntity>
}

@Dao
interface ExpenseDao {
    @Query(
        """
        SELECT * FROM expenses
        WHERE is_deleted = 0
        AND (:category IS NULL OR category = :category)
        AND (:searchQuery = '' OR description LIKE '%' || :searchQuery || '%' OR vendor LIKE '%' || :searchQuery || '%')
        AND (:startDate IS NULL OR expense_date >= :startDate)
        AND (:endDate IS NULL OR expense_date <= :endDate)
        ORDER BY expense_date DESC, created_at DESC
        """,
    )
    fun observeFiltered(
        searchQuery: String,
        category: ExpenseCategory?,
        startDate: Long?,
        endDate: Long?,
    ): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun observeById(id: String): Flow<ExpenseEntity?>

    @Query("SELECT * FROM expenses WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun getById(id: String): ExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(expenses: List<ExpenseEntity>)

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE sync_status != 'SYNCED' AND is_deleted = 0")
    suspend fun getPendingSync(): List<ExpenseEntity>

    @Query("SELECT COUNT(*) FROM expenses WHERE sync_status != 'SYNCED' AND is_deleted = 0")
    fun observePendingCount(): Flow<Int>

    @Query(
        """
        UPDATE expenses SET
        sync_status = :status,
        last_synced_at = :syncedAt,
        local_updated_at = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateSyncStatus(
        id: String,
        status: SyncStatus,
        syncedAt: Long? = null,
        updatedAt: Long = System.currentTimeMillis(),
    )
}

@Dao
interface DocumentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(document: com.krishifarms.mobile.core.database.entity.DocumentEntity)

    @Query("SELECT * FROM documents WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun getById(id: String): com.krishifarms.mobile.core.database.entity.DocumentEntity?

    @Query("SELECT * FROM documents WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun observeById(id: String): Flow<com.krishifarms.mobile.core.database.entity.DocumentEntity?>

    @Query(
        """
        SELECT * FROM documents
        WHERE document_type = :type
        AND is_deleted = 0
        ORDER BY created_at DESC
        """,
    )
    fun observeByType(
        type: com.krishifarms.mobile.feature.document.domain.model.DocumentType,
    ): Flow<List<com.krishifarms.mobile.core.database.entity.DocumentEntity>>

    @Query(
        """
        SELECT * FROM documents
        WHERE (:entityType IS NULL OR linked_entity_type = :entityType)
        AND (:entityId IS NULL OR linked_entity_id = :entityId)
        AND is_deleted = 0
        ORDER BY created_at DESC
        """,
    )
    fun observeByEntity(
        entityType: String?,
        entityId: String?,
    ): Flow<List<com.krishifarms.mobile.core.database.entity.DocumentEntity>>

    @Query(
        """
        SELECT * FROM documents
        WHERE sync_status != 'SYNCED'
        AND is_deleted = 0
        ORDER BY created_at ASC
        """,
    )
    suspend fun getPendingUpload(): List<com.krishifarms.mobile.core.database.entity.DocumentEntity>

    @Query(
        """
        UPDATE documents SET
        sync_status = :status,
        last_synced_at = :syncedAt,
        local_updated_at = :updatedAt,
        remote_url = COALESCE(:remoteUrl, remote_url),
        uploaded_at = COALESCE(:uploadedAt, uploaded_at),
        sync_error = :error
        WHERE id = :id
        """,
    )
    suspend fun updateSyncStatus(
        id: String,
        status: SyncStatus,
        syncedAt: Long? = null,
        updatedAt: Long = System.currentTimeMillis(),
        remoteUrl: String? = null,
        uploadedAt: Long? = null,
        error: String? = null,
    )
}

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(operation: SyncOperationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncOperationEntity): Long = insert(item)

    @Query(
        """
        SELECT * FROM sync_queue
        WHERE status IN ('PENDING', 'FAILED')
        AND retry_count < max_retries
        ORDER BY priority DESC, created_at ASC
        """,
    )
    suspend fun getPending(): List<SyncOperationEntity>

    @Query("SELECT * FROM sync_queue WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SyncOperationEntity?

    @Query("SELECT * FROM sync_queue WHERE idempotency_key = :key LIMIT 1")
    suspend fun getByIdempotencyKey(key: String): SyncOperationEntity?

    @Query(
        """
        SELECT COUNT(*) FROM sync_queue
        WHERE status IN ('PENDING', 'IN_PROGRESS', 'FAILED', 'CONFLICT')
        """,
    )
    fun observePendingCount(): Flow<Int>

    @Query(
        """
        SELECT * FROM sync_queue
        WHERE status IN ('PENDING', 'FAILED', 'CONFLICT')
        ORDER BY created_at DESC
        """,
    )
    fun observeUnresolved(): Flow<List<SyncOperationEntity>>

    @Query(
        """
        SELECT * FROM sync_queue
        WHERE status IN ('PENDING', 'FAILED', 'CONFLICT')
        ORDER BY created_at ASC
        """,
    )
    fun observePending(): Flow<List<SyncOperationEntity>>

    @Query("UPDATE sync_queue SET status = 'IN_PROGRESS', last_attempt_at = :attemptAt WHERE id = :id")
    suspend fun markInProgress(id: Long, attemptAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_queue SET status = 'SUCCESS', last_attempt_at = :attemptAt, error_message = NULL WHERE id = :id")
    suspend fun markSuccess(id: Long, attemptAt: Long = System.currentTimeMillis())

    @Query(
        """
        UPDATE sync_queue
        SET status = 'FAILED', retry_count = retry_count + 1, last_attempt_at = :attemptAt, error_message = :error
        WHERE id = :id
        """,
    )
    suspend fun markFailed(id: Long, error: String, attemptAt: Long = System.currentTimeMillis())

    @Query(
        """
        UPDATE sync_queue
        SET status = 'CONFLICT', last_attempt_at = :attemptAt, error_message = :error
        WHERE id = :id
        """,
    )
    suspend fun markConflict(id: Long, error: String, attemptAt: Long = System.currentTimeMillis())

    @Query("UPDATE sync_queue SET retry_count = retry_count + 1, last_attempt_at = :attemptAt WHERE id = :id")
    suspend fun incrementRetry(id: Long, attemptAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM sync_queue WHERE status = 'SUCCESS' AND created_at < :before")
    suspend fun pruneSuccessful(before: Long)
}

@Dao
interface SyncMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: SyncMetadataEntity)

    @Query("SELECT * FROM sync_metadata WHERE entity_type = :entityType LIMIT 1")
    suspend fun getByEntityType(entityType: com.krishifarms.mobile.core.sync.domain.SyncEntityType): SyncMetadataEntity?

    @Query("SELECT * FROM sync_metadata")
    fun observeAll(): Flow<List<SyncMetadataEntity>>
}

@Dao
interface SyncConflictDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conflict: SyncConflictEntity): Long

    @Query("SELECT * FROM sync_conflicts WHERE resolved = 0 ORDER BY created_at DESC")
    fun observeUnresolved(): Flow<List<SyncConflictEntity>>

    @Query("UPDATE sync_conflicts SET resolved = 1 WHERE id = :id")
    suspend fun markResolved(id: Long)
}
