package com.krishifarms.mobile.feature.document.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object DocumentDto {
    @Serializable
    data class DocumentResponse(
        val id: String,
        @SerialName("document_type")
        val documentType: String,
        @SerialName("file_name")
        val fileName: String,
        @SerialName("mime_type")
        val mimeType: String,
        val url: String,
        @SerialName("size_bytes")
        val sizeBytes: Long? = null,
        @SerialName("entity_type")
        val entityType: String? = null,
        @SerialName("entity_id")
        val entityId: String? = null,
        @SerialName("uploaded_at")
        val uploadedAt: Long,
        @SerialName("updated_at")
        val updatedAt: Long,
    )

    @Serializable
    data class UploadMetadata(
        @SerialName("document_type")
        val documentType: String,
        @SerialName("file_name")
        val fileName: String,
        @SerialName("mime_type")
        val mimeType: String,
        @SerialName("entity_type")
        val entityType: String? = null,
        @SerialName("entity_id")
        val entityId: String? = null,
        @SerialName("idempotency_key")
        val idempotencyKey: String,
    )

    @Serializable
    data class UploadResponseDto(
        val url: String,
        @SerialName("document_id")
        val documentId: String? = null,
    )

    @Serializable
    data class DocumentListResponse(
        val data: List<DocumentResponse>,
    )
}
