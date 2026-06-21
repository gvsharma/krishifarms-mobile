package com.krishifarms.mobile.core.network.interceptor

import com.krishifarms.mobile.core.common.NetworkMonitor
import com.krishifarms.mobile.feature.auth.data.local.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectivityInterceptor @Inject constructor(
    private val networkMonitor: NetworkMonitor,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!networkMonitor.isOnline()) {
            throw IOException("No internet connection")
        }
        return chain.proceed(chain.request())
    }
}

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = tokenStorage.getAccessToken()
        val request = if (accessToken.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        }
        return chain.proceed(request)
    }
}
