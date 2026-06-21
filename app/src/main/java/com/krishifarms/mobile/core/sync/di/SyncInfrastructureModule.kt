package com.krishifarms.mobile.core.sync.di

import android.content.Context
import androidx.work.WorkManager
import com.krishifarms.mobile.core.network.DocumentApiService
import com.krishifarms.mobile.core.network.ExpenseApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncInfrastructureModule {

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context,
    ): WorkManager = WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideExpenseApiService(
        @Named("authenticated_retrofit") retrofit: Retrofit,
    ): ExpenseApiService = retrofit.create(ExpenseApiService::class.java)

    @Provides
    @Singleton
    fun provideDocumentApiService(
        @Named("authenticated_retrofit") retrofit: Retrofit,
    ): DocumentApiService = retrofit.create(DocumentApiService::class.java)
}
