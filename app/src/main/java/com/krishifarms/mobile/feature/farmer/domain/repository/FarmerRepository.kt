package com.krishifarms.mobile.feature.farmer.domain.repository

import com.krishifarms.mobile.core.common.Resource
import com.krishifarms.mobile.feature.farmer.domain.model.Farmer
import kotlinx.coroutines.flow.Flow

data class FarmerInput(
    val name: String,
    val village: String,
    val phone: String,
    val bankDetails: String,
    val landAcres: Double,
    val cropTypes: List<String>,
)

interface FarmerRepository {
    fun getFarmers(searchQuery: String = ""): Flow<List<Farmer>>
    fun getFarmerById(id: String): Flow<Farmer?>
    suspend fun createFarmer(input: FarmerInput): Resource<Farmer>
    suspend fun updateFarmer(id: String, input: FarmerInput): Resource<Farmer>
    suspend fun syncFarmers(): Resource<Unit>
}
