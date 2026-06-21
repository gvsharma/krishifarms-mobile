package com.krishifarms.mobile.core.di

import android.content.Context
import androidx.room.Room
import com.krishifarms.mobile.core.database.KrishiFarmsDatabase
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
import com.krishifarms.mobile.feature.dashboard.data.local.dao.DashboardDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideKrishiFarmsDatabase(
        @ApplicationContext context: Context,
    ): KrishiFarmsDatabase = Room.databaseBuilder(
        context,
        KrishiFarmsDatabase::class.java,
        KrishiFarmsDatabase.DATABASE_NAME,
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides fun provideUserSessionDao(database: KrishiFarmsDatabase): UserSessionDao =
        database.userSessionDao()

    @Provides fun provideFarmerDao(database: KrishiFarmsDatabase): FarmerDao =
        database.farmerDao()

    @Provides fun provideFarmDao(database: KrishiFarmsDatabase): FarmDao =
        database.farmDao()

    @Provides fun provideProcurementDao(database: KrishiFarmsDatabase): ProcurementDao =
        database.procurementDao()

    @Provides fun providePaymentDao(database: KrishiFarmsDatabase): PaymentDao =
        database.paymentDao()

    @Provides fun provideWorkerDao(database: KrishiFarmsDatabase): WorkerDao =
        database.workerDao()

    @Provides fun provideWorkOrderDao(database: KrishiFarmsDatabase): WorkOrderDao =
        database.workOrderDao()

    @Provides fun provideAttendanceDao(database: KrishiFarmsDatabase): AttendanceDao =
        database.attendanceDao()

    @Provides fun provideExpenseDao(database: KrishiFarmsDatabase): ExpenseDao =
        database.expenseDao()

    @Provides fun provideDocumentDao(database: KrishiFarmsDatabase): DocumentDao =
        database.documentDao()

    @Provides fun provideSyncQueueDao(database: KrishiFarmsDatabase): SyncQueueDao =
        database.syncQueueDao()

    @Provides fun provideSyncMetadataDao(database: KrishiFarmsDatabase): SyncMetadataDao =
        database.syncMetadataDao()

    @Provides fun provideSyncConflictDao(database: KrishiFarmsDatabase): SyncConflictDao =
        database.syncConflictDao()

    @Provides fun provideDashboardDao(database: KrishiFarmsDatabase): DashboardDao =
        database.dashboardDao()
}
