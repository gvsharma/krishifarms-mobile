package com.krishifarms.mobile.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.krishifarms.mobile.core.common.SyncStatus
import com.krishifarms.mobile.feature.expense.domain.model.ExpenseCategory
import com.krishifarms.mobile.feature.expense.domain.model.PaymentMethod
import com.krishifarms.mobile.core.sync.domain.OperationStatus
import com.krishifarms.mobile.core.sync.domain.SyncEntityType
import com.krishifarms.mobile.core.sync.domain.SyncOperationType

@Entity(tableName = "farmers")
data class FarmerEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "server_id")
    val serverId: String? = null,
    val name: String,
    val village: String,
    val phone: String,
    @ColumnInfo(name = "bank_details")
    val bankDetails: String,
    @ColumnInfo(name = "land_acres")
    val landAcres: Double,
    @ColumnInfo(name = "crop_types")
    val cropTypes: String,
    @Embedded
    val sync: SyncMetadata = SyncMetadata(),
)

@Entity(tableName = "farms")
data class FarmEntity(
    @PrimaryKey
    val id: String,
    val farmerId: String,
    val name: String,
    val acreage: Double,
    val cropType: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @Embedded
    val sync: SyncMetadata = SyncMetadata(),
)

@Entity(
    tableName = "procurements",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = FarmerEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmer_id"],
            onDelete = androidx.room.ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        androidx.room.Index("farmer_id"),
        androidx.room.Index("sync_status"),
    ],
)
data class ProcurementEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "server_id")
    val serverId: String? = null,
    @ColumnInfo(name = "farmer_id")
    val farmerId: String,
    val crop: String,
    val village: String,
    val bags: Int,
    val weight: Double,
    val moisture: Double,
    val rate: Double,
    val deductions: Double,
    @ColumnInfo(name = "net_amount")
    val netAmount: Double,
    @ColumnInfo(name = "local_image_path")
    val localImagePath: String? = null,
    @ColumnInfo(name = "local_bill_path")
    val localBillPath: String? = null,
    @ColumnInfo(name = "remote_image_url")
    val remoteImageUrl: String? = null,
    @ColumnInfo(name = "remote_bill_url")
    val remoteBillUrl: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @Embedded
    val sync: SyncMetadata = SyncMetadata(),
)

@Entity(
    tableName = "expenses",
    indices = [
        androidx.room.Index("category"),
        androidx.room.Index("expense_date"),
        androidx.room.Index("sync_status"),
    ],
)
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "server_id")
    val serverId: String? = null,
    val category: ExpenseCategory,
    val amount: Double,
    val description: String,
    @ColumnInfo(name = "expense_date")
    val expenseDate: Long,
    val vendor: String? = null,
    @ColumnInfo(name = "payment_method")
    val paymentMethod: PaymentMethod? = null,
    @ColumnInfo(name = "local_bill_path")
    val localBillPath: String? = null,
    @ColumnInfo(name = "remote_bill_url")
    val remoteBillUrl: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @Embedded
    val sync: SyncMetadata = SyncMetadata(),
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey
    val id: String,
    val farmerId: String,
    val amount: Double,
    val paymentMode: String,
    val referenceNumber: String? = null,
    val paidAt: Long,
    @Embedded
    val sync: SyncMetadata = SyncMetadata(),
)

@Entity(
    tableName = "documents",
    indices = [
        androidx.room.Index("document_type"),
        androidx.room.Index("linked_entity_type", "linked_entity_id"),
        androidx.room.Index("sync_status"),
    ],
)
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "document_type")
    val documentType: com.krishifarms.mobile.feature.document.domain.model.DocumentType,
    @ColumnInfo(name = "local_path")
    val localPath: String,
    @ColumnInfo(name = "remote_url")
    val remoteUrl: String? = null,
    @ColumnInfo(name = "file_name")
    val fileName: String,
    @ColumnInfo(name = "mime_type")
    val mimeType: String,
    @ColumnInfo(name = "size_bytes")
    val sizeBytes: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "linked_entity_type")
    val linkedEntityType: String? = null,
    @ColumnInfo(name = "linked_entity_id")
    val linkedEntityId: String? = null,
    @ColumnInfo(name = "uploaded_at")
    val uploadedAt: Long? = null,
    @Embedded
    val sync: SyncMetadata = SyncMetadata(),
)

@Entity(tableName = "sync_queue")
data class SyncOperationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "entity_type")
    val entityType: SyncEntityType,
    @ColumnInfo(name = "entity_id")
    val entityId: String,
    @ColumnInfo(name = "operation_type")
    val operationType: SyncOperationType,
    @ColumnInfo(name = "payload_json")
    val payloadJson: String,
    @ColumnInfo(name = "idempotency_key")
    val idempotencyKey: String,
    val status: OperationStatus = OperationStatus.PENDING,
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    @ColumnInfo(name = "max_retries")
    val maxRetries: Int = DEFAULT_MAX_RETRIES,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_attempt_at")
    val lastAttemptAt: Long? = null,
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,
    val priority: Int = 0,
) {
    companion object {
        const val DEFAULT_MAX_RETRIES = 5
    }
}

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey
    @ColumnInfo(name = "entity_type")
    val entityType: SyncEntityType,
    @ColumnInfo(name = "last_sync_timestamp")
    val lastSyncTimestamp: Long = 0L,
)

@Entity(tableName = "sync_conflicts")
data class SyncConflictEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "operation_id")
    val operationId: Long,
    @ColumnInfo(name = "entity_type")
    val entityType: SyncEntityType,
    @ColumnInfo(name = "entity_id")
    val entityId: String,
    @ColumnInfo(name = "client_payload_json")
    val clientPayloadJson: String,
    @ColumnInfo(name = "server_payload_json")
    val serverPayloadJson: String,
    @ColumnInfo(name = "client_updated_at")
    val clientUpdatedAt: Long,
    @ColumnInfo(name = "server_updated_at")
    val serverUpdatedAt: Long,
    val resolved: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)
