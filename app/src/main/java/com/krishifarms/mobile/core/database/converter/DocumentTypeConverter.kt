package com.krishifarms.mobile.core.database.converter

import androidx.room.TypeConverter
import com.krishifarms.mobile.feature.document.domain.model.DocumentType

class DocumentTypeConverter {
    @TypeConverter
    fun fromDocumentType(type: DocumentType): String = type.name

    @TypeConverter
    fun toDocumentType(value: String): DocumentType = DocumentType.valueOf(value)
}
