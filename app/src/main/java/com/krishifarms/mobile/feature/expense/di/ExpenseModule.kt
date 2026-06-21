package com.krishifarms.mobile.feature.expense.di

import com.krishifarms.mobile.feature.expense.data.remote.ExpenseApi
import com.krishifarms.mobile.feature.expense.data.repository.ExpenseRepositoryImpl
import com.krishifarms.mobile.feature.expense.domain.repository.ExpenseRepository
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
abstract class ExpenseRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ExpenseApiModule {

    @Provides
    @Singleton
    fun provideExpenseApi(
        @Named("authenticated_retrofit") retrofit: Retrofit,
    ): ExpenseApi = retrofit.create(ExpenseApi::class.java)
}
