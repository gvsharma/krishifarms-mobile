package com.krishifarms.mobile.core.di

import com.krishifarms.mobile.BuildConfig
import com.krishifarms.mobile.core.network.interceptor.AuthInterceptor
import com.krishifarms.mobile.core.network.interceptor.ConnectivityInterceptor
import com.krishifarms.mobile.feature.auth.data.remote.AuthApi
import com.krishifarms.mobile.feature.dashboard.data.remote.DashboardApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val AUTH_CLIENT = "auth"
    private const val AUTH_RETROFIT = "auth_retrofit"
    private const val AUTHENTICATED_CLIENT = "authenticated"
    private const val AUTHENTICATED_RETROFIT = "authenticated_retrofit"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    @Named(AUTH_CLIENT)
    fun provideAuthOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @Named(AUTHENTICATED_CLIENT)
    fun provideAuthenticatedOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        connectivityInterceptor: ConnectivityInterceptor,
        authInterceptor: AuthInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(connectivityInterceptor)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @Named(AUTH_RETROFIT)
    fun provideAuthRetrofit(
        @Named(AUTH_CLIENT) okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = createRetrofit(okHttpClient, json)

    @Provides
    @Singleton
    @Named(AUTHENTICATED_RETROFIT)
    fun provideAuthenticatedRetrofit(
        @Named(AUTHENTICATED_CLIENT) authenticatedOkHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = createRetrofit(authenticatedOkHttpClient, json)

    @Provides
    @Singleton
    fun provideAuthApi(
        @Named(AUTH_RETROFIT) retrofit: Retrofit,
    ): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideDashboardApi(
        @Named(AUTHENTICATED_RETROFIT) retrofit: Retrofit,
    ): DashboardApi = retrofit.create(DashboardApi::class.java)

    private fun createRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}
