package com.krishifarms.mobile.feature.document.data.remote

import com.krishifarms.mobile.core.network.ApiResponse
import com.krishifarms.mobile.feature.document.data.remote.dto.DocumentDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface DocumentApi {
    @Multipart
    @POST("documents/upload")
    suspend fun uploadDocument(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Part("metadata") metadata: RequestBody,
        @Part file: MultipartBody.Part,
    ): ApiResponse<DocumentDto.UploadResponseDto>

    @GET("documents/{id}")
    suspend fun getDocument(
        @Path("id") id: String,
    ): ApiResponse<DocumentDto.DocumentResponse>

    @GET("documents")
    suspend fun listDocuments(
        @Query("type") type: String? = null,
        @Query("entity_type") entityType: String? = null,
        @Query("entity_id") entityId: String? = null,
    ): ApiResponse<DocumentDto.DocumentListResponse>
}
