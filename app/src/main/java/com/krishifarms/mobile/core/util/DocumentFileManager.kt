package com.krishifarms.mobile.core.util

import android.content.Context
import android.net.Uri
import com.krishifarms.mobile.feature.document.domain.model.DocumentType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class SavedDocumentFile(
    val path: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
)

@Singleton
class DocumentFileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageCompressor: ImageCompressor,
) {
    private val documentsDir: File
        get() = File(context.filesDir, DOCUMENTS_DIR).also { it.mkdirs() }

    fun createCaptureOutputFile(documentType: DocumentType): File {
        return File(documentsDir, "${documentType.storagePrefix}_${UUID.randomUUID()}.jpg")
    }

    suspend fun saveFromUri(uri: Uri, documentType: DocumentType): SavedDocumentFile {
        val tempFile = File(documentsDir, "${documentType.storagePrefix}_raw_${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output -> input.copyTo(output) }
        } ?: error("Unable to read selected file")

        return compressAndFinalize(tempFile, documentType)
    }

    suspend fun saveFromPath(sourcePath: String, documentType: DocumentType): SavedDocumentFile {
        val sourceFile = File(sourcePath)
        require(sourceFile.exists()) { "Source file not found: $sourcePath" }
        return compressAndFinalize(sourceFile, documentType)
    }

    fun fileForPath(path: String): File = File(path)

    fun deleteIfExists(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }

    private suspend fun compressAndFinalize(
        sourceFile: File,
        documentType: DocumentType,
    ): SavedDocumentFile {
        val outputFile = File(documentsDir, "${documentType.storagePrefix}_${UUID.randomUUID()}.jpg")
        val compressed = imageCompressor.compress(
            sourcePath = sourceFile.absolutePath,
            outputPath = outputFile.absolutePath,
        )
        if (sourceFile.absolutePath != outputFile.absolutePath) {
            sourceFile.delete()
        }
        return SavedDocumentFile(
            path = compressed.outputPath,
            fileName = outputFile.name,
            mimeType = compressed.mimeType,
            sizeBytes = compressed.sizeBytes,
        )
    }

    companion object {
        const val DOCUMENTS_DIR = "documents"
    }
}
