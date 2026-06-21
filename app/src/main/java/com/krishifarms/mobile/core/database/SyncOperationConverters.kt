package com.krishifarms.mobile.core.database

import androidx.room.TypeConverter
import com.krishifarms.mobile.core.sync.domain.OperationStatus
import com.krishifarms.mobile.core.sync.domain.SyncEntityType
import com.krishifarms.mobile.core.sync.domain.SyncOperationType

class SyncOperationConverters {
    @TypeConverter
    fun fromOperationStatus(status: OperationStatus): String = status.name

    @TypeConverter
    fun toOperationStatus(value: String): OperationStatus = OperationStatus.valueOf(value)

    @TypeConverter
    fun fromSyncOperationType(type: SyncOperationType): String = type.name

    @TypeConverter
    fun toSyncOperationType(value: String): SyncOperationType = SyncOperationType.valueOf(value)

    @TypeConverter
    fun fromSyncEntityType(type: SyncEntityType): String = type.name

    @TypeConverter
    fun toSyncEntityType(value: String): SyncEntityType = SyncEntityType.valueOf(value)
}
