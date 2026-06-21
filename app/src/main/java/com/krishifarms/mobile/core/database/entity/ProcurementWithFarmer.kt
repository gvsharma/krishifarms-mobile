package com.krishifarms.mobile.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class ProcurementWithFarmer(
    @Embedded
    val procurement: ProcurementEntity,
    @ColumnInfo(name = "farmer_name")
    val farmerName: String,
    @ColumnInfo(name = "farmer_village")
    val farmerVillage: String,
)
