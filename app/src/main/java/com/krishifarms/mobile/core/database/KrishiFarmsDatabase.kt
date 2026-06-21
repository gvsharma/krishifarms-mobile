package com.krishifarms.mobile.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.krishifarms.mobile.core.data.local.entity.UserSessionEntity
import com.krishifarms.mobile.core.database.converter.DocumentTypeConverter
import com.krishifarms.mobile.core.database.converter.ExpenseCategoryConverter
import com.krishifarms.mobile.core.database.converter.PaymentMethodConverter
import com.krishifarms.mobile.core.database.dao.AttendanceDao
import com.krishifarms.mobile.core.database.dao.DocumentDao
import com.krishifarms.mobile.core.database.dao.ExpenseDao
import com.krishifarms.mobile.core.database.dao.FarmDao
import com.krishifarms.mobile.core.database.dao.FarmerDao
import com.krishifarms.mobile.core.database.dao.PaymentDao
import com.krishifarms.mobile.core.database.dao.ProcurementDao
import com.krishifarms.mobile.core.database.dao.SyncConflictDao
import com.krishifarms.mobile.core.database.dao.SyncMetadataDao
import com.krishifarms.mobile.core.database.dao.SyncQueueDao
import com.krishifarms.mobile.core.database.dao.UserSessionDao
import com.krishifarms.mobile.core.database.dao.WorkOrderDao
import com.krishifarms.mobile.core.database.dao.WorkerDao
import com.krishifarms.mobile.core.database.entity.AttendanceEntity
import com.krishifarms.mobile.core.database.entity.DocumentEntity
import com.krishifarms.mobile.core.database.entity.ExpenseEntity
import com.krishifarms.mobile.core.database.entity.FarmEntity
import com.krishifarms.mobile.core.database.entity.FarmerEntity
import com.krishifarms.mobile.core.database.entity.PaymentEntity
import com.krishifarms.mobile.core.database.entity.ProcurementEntity
import com.krishifarms.mobile.core.database.entity.SyncConflictEntity
import com.krishifarms.mobile.core.database.entity.SyncMetadataEntity
import com.krishifarms.mobile.core.database.entity.SyncOperationEntity
import com.krishifarms.mobile.core.database.entity.WorkOrderEntity
import com.krishifarms.mobile.core.database.entity.WorkerEntity
import com.krishifarms.mobile.feature.dashboard.data.local.dao.DashboardDao
import com.krishifarms.mobile.feature.dashboard.data.local.entity.DashboardSummaryEntity

@Database(
    entities = [
        UserSessionEntity::class,
        FarmerEntity::class,
        FarmEntity::class,
        ProcurementEntity::class,
        PaymentEntity::class,
        WorkerEntity::class,
        WorkOrderEntity::class,
        AttendanceEntity::class,
        ExpenseEntity::class,
        DocumentEntity::class,
        SyncOperationEntity::class,
        SyncMetadataEntity::class,
        SyncConflictEntity::class,
        DashboardSummaryEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
@TypeConverters(
    SyncStatusConverter::class,
    SyncOperationConverters::class,
    ExpenseCategoryConverter::class,
    PaymentMethodConverter::class,
    DocumentTypeConverter::class,
)
abstract class KrishiFarmsDatabase : RoomDatabase() {
    abstract fun userSessionDao(): UserSessionDao
    abstract fun farmerDao(): FarmerDao
    abstract fun farmDao(): FarmDao
    abstract fun procurementDao(): ProcurementDao
    abstract fun paymentDao(): PaymentDao
    abstract fun workerDao(): WorkerDao
    abstract fun workOrderDao(): WorkOrderDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun documentDao(): DocumentDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun syncMetadataDao(): SyncMetadataDao
    abstract fun syncConflictDao(): SyncConflictDao
    abstract fun dashboardDao(): DashboardDao

    companion object {
        const val DATABASE_NAME = "krishifarms.db"
    }
}
