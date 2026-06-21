package com.krishifarms.mobile.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object AuthDtos {
    @Serializable
    data class SendOtpRequest(
        val phone: String,
    )

    @Serializable
    data class VerifyOtpRequest(
        val phone: String,
        val otp: String,
    )

    @Serializable
    data class RefreshTokenRequest(
        @SerialName("refresh_token")
        val refreshToken: String,
    )

    @Serializable
    data class OtpResponse(
        val success: Boolean,
        val message: String,
        @SerialName("expires_in")
        val expiresIn: Int? = null,
    )

    @Serializable
    data class AuthResponse(
        @SerialName("access_token")
        val accessToken: String,
        @SerialName("refresh_token")
        val refreshToken: String,
        val user: UserDto,
    )

    @Serializable
    data class UserDto(
        val id: String,
        val name: String,
        val phone: String,
        val role: String,
        val region: String? = null,
    )
}

object FarmerDtos {
    @Serializable
    data class FarmerListResponse(
        val items: List<FarmerDto>,
        val page: Int = 1,
        @SerialName("page_size") val pageSize: Int = 50,
        @SerialName("total_items") val totalItems: Int = 0,
        @SerialName("total_pages") val totalPages: Int = 1,
    )

    @Serializable
    data class FarmerDto(
        val id: String,
        val name: String,
        val phone: String,
        val village: String,
        @SerialName("bank_details") val bankDetails: String,
        @SerialName("land_acres") val landAcres: Double,
        @SerialName("crop_types") val cropTypes: List<String>,
        @SerialName("updated_at") val updatedAt: Long,
    )

    @Serializable
    data class CreateFarmerRequest(
        val name: String,
        val phone: String,
        val village: String,
        @SerialName("bank_details") val bankDetails: String,
        @SerialName("land_acres") val landAcres: Double,
        @SerialName("crop_types") val cropTypes: List<String>,
    )

    @Serializable
    data class UpdateFarmerRequest(
        val name: String,
        val phone: String,
        val village: String,
        @SerialName("bank_details") val bankDetails: String,
        @SerialName("land_acres") val landAcres: Double,
        @SerialName("crop_types") val cropTypes: List<String>,
    )
}

object FarmDtos {
    @Serializable
    data class FarmListResponse(
        val data: List<FarmDto>,
        val page: Int,
    )

    @Serializable
    data class FarmDto(
        val id: String,
        @SerialName("farmer_id")
        val farmerId: String,
        val name: String,
        val acreage: Double,
        @SerialName("crop_type")
        val cropType: String,
        val latitude: Double? = null,
        val longitude: Double? = null,
        @SerialName("updated_at")
        val updatedAt: Long,
    )
}

object ProcurementDtos {
    @Serializable
    data class ProcurementListResponse(
        val data: List<ProcurementDto>,
        val page: Int,
    )

    @Serializable
    data class ProcurementDto(
        val id: String,
        @SerialName("farmer_id")
        val farmerId: String,
        @SerialName("farm_id")
        val farmId: String,
        @SerialName("crop_name")
        val cropName: String,
        @SerialName("quantity_kg")
        val quantityKg: Double,
        @SerialName("rate_per_kg")
        val ratePerKg: Double,
        @SerialName("total_amount")
        val totalAmount: Double,
        @SerialName("procured_at")
        val procuredAt: Long,
        @SerialName("updated_at")
        val updatedAt: Long,
    )

    @Serializable
    data class CreateProcurementRequest(
        @SerialName("farmer_id")
        val farmerId: String,
        @SerialName("farm_id")
        val farmId: String,
        @SerialName("crop_name")
        val cropName: String,
        @SerialName("quantity_kg")
        val quantityKg: Double,
        @SerialName("rate_per_kg")
        val ratePerKg: Double,
        @SerialName("procured_at")
        val procuredAt: Long,
    )
}

object PaymentDtos {
    @Serializable
    data class PaymentListResponse(
        val data: List<PaymentDto>,
        val page: Int,
    )

    @Serializable
    data class PaymentDto(
        val id: String,
        @SerialName("farmer_id")
        val farmerId: String,
        val amount: Double,
        @SerialName("payment_mode")
        val paymentMode: String,
        @SerialName("reference_number")
        val referenceNumber: String? = null,
        @SerialName("paid_at")
        val paidAt: Long,
        @SerialName("updated_at")
        val updatedAt: Long,
    )
}

object ExpenseDtos {
    @Serializable
    data class ExpenseListResponse(
        val data: List<ExpenseDto>,
        val page: Int,
    )

    @Serializable
    data class ExpenseDto(
        val id: String,
        val category: String,
        val description: String,
        val amount: Double,
        @SerialName("expense_date")
        val expenseDate: Long,
        @SerialName("spent_at")
        val spentAt: Long,
        val vendor: String? = null,
        @SerialName("payment_method")
        val paymentMethod: String? = null,
        @SerialName("bill_url")
        val billUrl: String? = null,
        @SerialName("created_at")
        val createdAt: Long,
        @SerialName("updated_at")
        val updatedAt: Long,
    )

    @Serializable
    data class CreateExpenseRequest(
        val category: String,
        val description: String,
        val amount: Double,
        @SerialName("expense_date")
        val expenseDate: Long? = null,
        @SerialName("spent_at")
        val spentAt: Long,
        val vendor: String? = null,
        @SerialName("payment_method")
        val paymentMethod: String? = null,
        @SerialName("farm_id")
        val farmId: String? = null,
        @SerialName("idempotency_key")
        val idempotencyKey: String,
    )

    @Serializable
    data class UpdateExpenseRequest(
        val category: String? = null,
        val description: String? = null,
        val amount: Double? = null,
        @SerialName("expense_date")
        val expenseDate: Long? = null,
        @SerialName("spent_at")
        val spentAt: Long? = null,
        val vendor: String? = null,
        @SerialName("payment_method")
        val paymentMethod: String? = null,
        @SerialName("farm_id")
        val farmId: String? = null,
    )
}

object DocumentDtos {
    @Serializable
    data class DocumentDto(
        val id: String,
        @SerialName("document_type")
        val documentType: String? = null,
        @SerialName("entity_type")
        val entityType: String? = null,
        @SerialName("entity_id")
        val entityId: String? = null,
        @SerialName("file_name")
        val fileName: String,
        @SerialName("mime_type")
        val mimeType: String,
        val url: String,
        @SerialName("uploaded_at")
        val uploadedAt: Long,
        @SerialName("updated_at")
        val updatedAt: Long,
    )

    @Serializable
    data class UploadDocumentRequest(
        @SerialName("document_type")
        val documentType: String,
        @SerialName("entity_type")
        val entityType: String? = null,
        @SerialName("entity_id")
        val entityId: String? = null,
        @SerialName("file_name")
        val fileName: String,
        @SerialName("mime_type")
        val mimeType: String,
        @SerialName("idempotency_key")
        val idempotencyKey: String,
    )

    @Serializable
    data class UploadDocumentResponse(
        val document: DocumentDto,
    )
}

object WorkerDtos {
    @Serializable
    data class WorkerListResponse(
        val data: List<WorkerDto>,
        val page: Int,
    )

    @Serializable
    data class WorkerDto(
        val id: String,
        val name: String,
        val phone: String,
        val role: String,
        @SerialName("assigned_region")
        val assignedRegion: String,
        val active: Boolean = true,
        @SerialName("updated_at")
        val updatedAt: Long,
    )

    @Serializable
    data class CreateWorkerRequest(
        val name: String,
        val phone: String,
        val role: String,
        @SerialName("assigned_region")
        val assignedRegion: String,
    )

    @Serializable
    data class UpdateWorkerRequest(
        val name: String? = null,
        val phone: String? = null,
        val role: String? = null,
        @SerialName("assigned_region")
        val assignedRegion: String? = null,
        val active: Boolean? = null,
    )
}

object SyncDtos {
    @Serializable
    data class PushRequest(
        val changes: List<SyncChangeDto>,
    )

    @Serializable
    data class SyncChangeDto(
        @SerialName("entity_type")
        val entityType: String,
        @SerialName("entity_id")
        val entityId: String,
        val operation: String,
        val payload: String? = null,
    )

    @Serializable
    data class PushResponse(
        val accepted: List<String>,
        val rejected: List<SyncRejectionDto>,
    )

    @Serializable
    data class SyncRejectionDto(
        @SerialName("entity_id")
        val entityId: String,
        val reason: String,
    )

    @Serializable
    data class PullResponse(
        val changes: List<SyncChangeDto>,
        @SerialName("server_timestamp")
        val serverTimestamp: Long,
    )
}
