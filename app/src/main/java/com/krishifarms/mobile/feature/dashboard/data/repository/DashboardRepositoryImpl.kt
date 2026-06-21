package com.krishifarms.mobile.feature.dashboard.data.repository

import com.krishifarms.mobile.core.common.NetworkMonitor
import com.krishifarms.mobile.core.common.Resource
import com.krishifarms.mobile.core.network.NetworkResult
import com.krishifarms.mobile.core.network.safeApiCall
import com.krishifarms.mobile.feature.dashboard.data.local.dao.DashboardDao
import com.krishifarms.mobile.feature.dashboard.data.mapper.toDomain
import com.krishifarms.mobile.feature.dashboard.data.mapper.toEntity
import com.krishifarms.mobile.feature.dashboard.data.remote.DashboardApi
import com.krishifarms.mobile.feature.dashboard.domain.model.DashboardSummary
import com.krishifarms.mobile.feature.dashboard.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val dashboardApi: DashboardApi,
    private val dashboardDao: DashboardDao,
    private val networkMonitor: NetworkMonitor,
) : DashboardRepository {

    override fun observeSummary(): Flow<DashboardSummary?> {
        return dashboardDao.observeSummary().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun refresh(forceRemote: Boolean): Resource<DashboardSummary> {
        val cached = dashboardDao.getSummary()?.toDomain()

        if (!forceRemote && cached != null && !networkMonitor.isOnline()) {
            return Resource.Success(cached)
        }

        if (!networkMonitor.isOnline()) {
            return if (cached != null) {
                Resource.Success(cached)
            } else {
                Resource.Error("No internet connection. Pull to refresh when online.")
            }
        }

        return when (val result = safeApiCall { dashboardApi.getSummary() }) {
            is NetworkResult.Success -> {
                val summary = result.data.data.toDomain()
                dashboardDao.upsert(summary.toEntity())
                Resource.Success(summary)
            }

            is NetworkResult.Error -> {
                if (cached != null) {
                    Resource.Success(cached.copy(isFromCache = true))
                } else {
                    Resource.Error(result.message)
                }
            }
        }
    }

    override suspend fun clearCache() {
        dashboardDao.clear()
    }
}
