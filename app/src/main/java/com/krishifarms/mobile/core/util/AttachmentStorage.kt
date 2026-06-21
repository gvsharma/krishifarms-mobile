package com.krishifarms.mobile.core.util

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val attachmentsDir: File
        get() = File(context.filesDir, "attachments").also { it.mkdirs() }

    fun createCameraOutputFile(prefix: String): File {
        return File(attachmentsDir, "${prefix}_${UUID.randomUUID()}.jpg")
    }

    fun saveFromUri(uri: Uri, prefix: String): String {
        val destination = File(attachmentsDir, "${prefix}_${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to read selected file")
        return destination.absolutePath
    }

    fun fileForPath(path: String): File = File(path)

    fun deleteIfExists(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }
}

object ProcurementCalculator {
    fun netAmount(weight: Double, rate: Double, deductions: Double): Double {
        val gross = weight * rate
        return (gross - deductions).coerceAtLeast(0.0)
    }
}
