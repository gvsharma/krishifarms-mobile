package com.krishifarms.mobile.feature.document.domain.repository

import android.net.Uri
import com.krishifarms.mobile.feature.document.domain.model.Document
import com.krishifarms.mobile.feature.document.domain.model.DocumentType
import kotlinx.coroutines.flow.Flow

data class AddDocumentInput(
    val type: DocumentType,
    val sourceUri: Uri? = null,
    val sourcePath: String? = null,
    val linkedEntityType: String? = null,
    val linkedEntityId: String? = null,
)

sealed interface DocumentUploadResult {
    data object Success : DocumentUploadResult
    data object Retry : DocumentUploadResult
    data class Failure(val message: String) : DocumentUploadResult
}

interface DocumentRepository {
    fun observeDocument(id: String): Flow<Document?>

    fun observeByType(type: DocumentType): Flow<List<Document>>

    fun observeByEntity(entityType: String?, entityId: String?): Flow<List<Document>>

    suspend fun addDocument(input: AddDocumentInput): Result<String>

    suspend fun uploadDocument(documentId: String): DocumentUploadResult

    suspend fun syncPendingUploads(): Int

    suspend fun getDocument(id: String): Document?
}
