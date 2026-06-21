package com.krishifarms.mobile.feature.worker.di

import com.krishifarms.mobile.core.network.AttendanceApiService
import com.krishifarms.mobile.core.network.WorkOrderApiService
import com.krishifarms.mobile.core.network.WorkerApiService
import com.krishifarms.mobile.feature.worker.data.repository.AttendanceRepositoryImpl
import com.krishifarms.mobile.feature.worker.data.repository.FarmLookupRepositoryImpl
import com.krishifarms.mobile.feature.worker.data.repository.WorkOrderRepositoryImpl
import com.krishifarms.mobile.feature.worker.data.repository.WorkerRepositoryImpl
import com.krishifarms.mobile.feature.worker.domain.repository.AttendanceRepository
import com.krishifarms.mobile.feature.worker.domain.repository.FarmLookupRepository
import com.krishifarms.mobile.feature.worker.domain.repository.WorkOrderRepository
import com.krishifarms.mobile.feature.worker.domain.repository.WorkerRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWorkerRepository(impl: WorkerRepositoryImpl): WorkerRepository

    @Binds
    @Singleton
    abstract fun bindWorkOrderRepository(impl: WorkOrderRepositoryImpl): WorkOrderRepository

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(impl: AttendanceRepositoryImpl): AttendanceRepository

    @Binds
    @Singleton
    abstract fun bindFarmLookupRepository(impl: FarmLookupRepositoryImpl): FarmLookupRepository
}

@Module
@InstallIn(SingletonComponent::class)
object WorkerApiModule {

    @Provides
    @Singleton
    fun provideWorkerApi(@Named("authenticated_retrofit") retrofit: Retrofit): WorkerApiService =
        retrofit.create(WorkerApiService::class.java)

    @Provides
    @Singleton
    fun provideWorkOrderApi(@Named("authenticated_retrofit") retrofit: Retrofit): WorkOrderApiService =
        retrofit.create(WorkOrderApiService::class.java)

    @Provides
    @Singleton
    fun provideAttendanceApi(@Named("authenticated_retrofit") retrofit: Retrofit): AttendanceApiService =
        retrofit.create(AttendanceApiService::class.java)
}
