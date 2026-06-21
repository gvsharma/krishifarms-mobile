package com.krishifarms.mobile.feature.farmer.domain.model

import com.krishifarms.mobile.core.common.SyncStatus

data class Farmer(
    val id: String,
    val serverId: String? = null,
    val name: String,
    val village: String,
    val phone: String,
    val bankDetails: String,
    val landAcres: Double,
    val cropTypes: List<String>,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

object AvailableCropTypes {
    val ALL = listOf(
        "Rice",
        "Cotton",
        "Maize",
        "Chilli",
        "Turmeric",
        "Groundnut",
        "Soybean",
        "Wheat",
        "Sugarcane",
        "Pulses",
        "Sunflower",
        "Bajra",
    )
}
