package com.krishifarms.mobile.feature.dashboard.domain.repository

import com.krishifarms.mobile.core.common.Resource
import com.krishifarms.mobile.feature.dashboard.domain.model.DashboardSummary
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun observeSummary(): Flow<DashboardSummary?>
    suspend fun refresh(forceRemote: Boolean = false): Resource<DashboardSummary>
    suspend fun clearCache()
}
