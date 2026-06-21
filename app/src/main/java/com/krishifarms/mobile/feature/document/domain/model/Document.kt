package com.krishifarms.mobile.feature.document.domain.model

import com.krishifarms.mobile.core.common.SyncStatus

data class Document(
    val id: String,
    val type: DocumentType,
    val localPath: String,
    val remoteUrl: String? = null,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val createdAt: Long,
    val syncStatus: SyncStatus,
    val linkedEntityType: String? = null,
    val linkedEntityId: String? = null,
    val uploadedAt: Long? = null,
)
