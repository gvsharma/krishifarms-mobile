package com.krishifarms.mobile.feature.procurement.data.remote

import com.krishifarms.mobile.core.network.ApiResponse
import com.krishifarms.mobile.core.network.PaginatedResponse
import com.krishifarms.mobile.core.network.UploadResponseDto
import com.krishifarms.mobile.feature.procurement.data.remote.dto.CreateProcurementRequest
import com.krishifarms.mobile.feature.procurement.data.remote.dto.CreateProcurementResponse
import com.krishifarms.mobile.feature.procurement.data.remote.dto.ProcurementDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ProcurementApi {
    @GET("procurements")
    suspend fun getProcurements(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
    ): ApiResponse<PaginatedResponse<ProcurementDto>>

    @GET("procurements/{id}")
    suspend fun getProcurementById(
        @Path("id") id: String,
    ): ApiResponse<ProcurementDto>

    @POST("procurements")
    suspend fun createProcurement(
        @Body request: CreateProcurementRequest,
    ): ApiResponse<CreateProcurementResponse>

    @Multipart
    @POST("procurements/{id}/image")
    suspend fun uploadImage(
        @Path("id") id: String,
        @Part image: MultipartBody.Part,
    ): ApiResponse<UploadResponseDto>

    @Multipart
    @POST("procurements/{id}/bill")
    suspend fun uploadBill(
        @Path("id") id: String,
        @Part bill: MultipartBody.Part,
    ): ApiResponse<UploadResponseDto>
}
