package com.krishifarms.mobile.feature.farmer.di

import com.krishifarms.mobile.core.network.FarmerApiService
import com.krishifarms.mobile.feature.farmer.data.repository.FarmerRepositoryImpl
import com.krishifarms.mobile.feature.farmer.domain.repository.FarmerRepository
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
abstract class FarmerModule {

    @Binds
    @Singleton
    abstract fun bindFarmerRepository(impl: FarmerRepositoryImpl): FarmerRepository

    companion object {
        @Provides
        @Singleton
        fun provideFarmerApi(
            @Named("authenticated_retrofit") retrofit: Retrofit,
        ): FarmerApiService = retrofit.create(FarmerApiService::class.java)
    }
}
