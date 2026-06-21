package com.krishifarms.mobile.feature.worker.data.repository

import com.krishifarms.mobile.core.database.dao.FarmDao
import com.krishifarms.mobile.feature.worker.domain.model.FarmOption
import com.krishifarms.mobile.feature.worker.domain.repository.FarmLookupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmLookupRepositoryImpl @Inject constructor(
    private val farmDao: FarmDao,
) : FarmLookupRepository {

    override fun observeFarms(): Flow<List<FarmOption>> =
        farmDao.observeAll().map { farms ->
            farms.map { FarmOption(id = it.id, name = it.name) }
        }
}
