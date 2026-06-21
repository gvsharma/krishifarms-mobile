package com.krishifarms.mobile.feature.procurement.di

import com.krishifarms.mobile.feature.procurement.data.remote.ProcurementApi
import com.krishifarms.mobile.feature.procurement.data.repository.ProcurementRepositoryImpl
import com.krishifarms.mobile.feature.procurement.domain.repository.ProcurementRepository
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
abstract class ProcurementRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProcurementRepository(impl: ProcurementRepositoryImpl): ProcurementRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ProcurementApiModule {

    @Provides
    @Singleton
    fun provideProcurementApi(
        @Named("authenticated_retrofit") retrofit: Retrofit,
    ): ProcurementApi = retrofit.create(ProcurementApi::class.java)
}
