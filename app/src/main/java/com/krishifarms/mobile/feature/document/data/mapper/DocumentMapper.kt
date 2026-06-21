package com.krishifarms.mobile.feature.document.data.mapper

import com.krishifarms.mobile.core.database.entity.DocumentEntity
import com.krishifarms.mobile.feature.document.domain.model.Document

fun DocumentEntity.toDomain(): Document = Document(
    id = id,
    type = documentType,
    localPath = localPath,
    remoteUrl = remoteUrl,
    fileName = fileName,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    createdAt = createdAt,
    syncStatus = sync.syncStatus,
    linkedEntityType = linkedEntityType,
    linkedEntityId = linkedEntityId,
    uploadedAt = uploadedAt,
)
