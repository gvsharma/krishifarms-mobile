package com.krishifarms.mobile.feature.document.domain.model

import androidx.annotation.StringRes
import com.krishifarms.mobile.R

enum class DocumentType {
    FUEL_BILL,
    CROP_BILL,
    RECEIPT,
    UPI_SCREENSHOT,
    PHOTO,
    ;

    val storagePrefix: String
        get() = name.lowercase()

    val apiValue: String
        get() = name.lowercase()

    @StringRes
    fun labelRes(): Int = when (this) {
        FUEL_BILL -> R.string.document_type_fuel_bill
        CROP_BILL -> R.string.document_type_crop_bill
        RECEIPT -> R.string.document_type_receipt
        UPI_SCREENSHOT -> R.string.document_type_upi_screenshot
        PHOTO -> R.string.document_type_photo
    }
}
