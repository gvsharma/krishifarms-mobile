package com.krishifarms.mobile.feature.dashboard.data.remote

import com.krishifarms.mobile.core.network.ApiResponse
import com.krishifarms.mobile.feature.dashboard.data.dto.DashboardSummaryResponse
import retrofit2.http.GET

interface DashboardApi {

    @GET("dashboard/summary")
    suspend fun getSummary(): ApiResponse<DashboardSummaryResponse>
}
