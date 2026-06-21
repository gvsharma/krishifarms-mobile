package com.krishifarms.mobile.feature.document.di

import com.krishifarms.mobile.feature.document.data.remote.DocumentApi
import com.krishifarms.mobile.feature.document.data.repository.DocumentRepositoryImpl
import com.krishifarms.mobile.feature.document.domain.repository.DocumentRepository
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
abstract class DocumentRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDocumentRepository(impl: DocumentRepositoryImpl): DocumentRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DocumentApiModule {
    @Provides
    @Singleton
    fun provideDocumentApi(
        @Named("authenticated_retrofit") retrofit: Retrofit,
    ): DocumentApi = retrofit.create(DocumentApi::class.java)
}
