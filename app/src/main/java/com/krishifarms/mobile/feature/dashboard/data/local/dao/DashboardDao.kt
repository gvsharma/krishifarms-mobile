package com.krishifarms.mobile.feature.dashboard.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.krishifarms.mobile.feature.dashboard.data.local.entity.DashboardSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {

    @Query("SELECT * FROM dashboard_summary_cache WHERE id = :id LIMIT 1")
    fun observeSummary(id: Int = DashboardSummaryEntity.SINGLETON_ID): Flow<DashboardSummaryEntity?>

    @Query("SELECT * FROM dashboard_summary_cache WHERE id = :id LIMIT 1")
    suspend fun getSummary(id: Int = DashboardSummaryEntity.SINGLETON_ID): DashboardSummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DashboardSummaryEntity)

    @Query("DELETE FROM dashboard_summary_cache")
    suspend fun clear()
}
