package com.krishifarms.mobile.feature.dashboard.di

import com.krishifarms.mobile.feature.dashboard.data.repository.DashboardRepositoryImpl
import com.krishifarms.mobile.feature.dashboard.domain.repository.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardModule {

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        impl: DashboardRepositoryImpl,
    ): DashboardRepository
}
