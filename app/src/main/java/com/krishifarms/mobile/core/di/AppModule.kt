package com.krishifarms.mobile.core.di

import com.krishifarms.mobile.core.common.DefaultDispatcherProvider
import com.krishifarms.mobile.core.common.DefaultNetworkMonitor
import com.krishifarms.mobile.core.common.DispatcherProvider
import com.krishifarms.mobile.core.common.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppBindingsModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(impl: DefaultNetworkMonitor): NetworkMonitor
}
