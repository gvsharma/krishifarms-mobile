package com.krishifarms.mobile.feature.auth.di

import com.krishifarms.mobile.core.network.TokenRefreshManager
import com.krishifarms.mobile.core.network.TokenRefresher
import com.krishifarms.mobile.feature.auth.data.local.AuthPreferences
import com.krishifarms.mobile.feature.auth.data.local.AuthPreferencesImpl
import com.krishifarms.mobile.feature.auth.data.local.EncryptedTokenStorage
import com.krishifarms.mobile.feature.auth.data.local.TokenStorage
import com.krishifarms.mobile.feature.auth.data.repository.AuthRepositoryImpl
import com.krishifarms.mobile.feature.auth.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindTokenStorage(
        impl: EncryptedTokenStorage,
    ): TokenStorage

    @Binds
    @Singleton
    abstract fun bindAuthPreferences(
        impl: AuthPreferencesImpl,
    ): AuthPreferences

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTokenRefresher(
        impl: TokenRefreshManager,
    ): TokenRefresher
}
