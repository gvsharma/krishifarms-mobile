package com.krishifarms.mobile.feature.expense.data.remote

import com.krishifarms.mobile.core.network.ApiResponse
import com.krishifarms.mobile.core.network.PaginatedResponse
import com.krishifarms.mobile.core.network.UploadResponseDto
import com.krishifarms.mobile.feature.expense.data.remote.dto.CreateExpenseRequest
import com.krishifarms.mobile.feature.expense.data.remote.dto.CreateExpenseResponse
import com.krishifarms.mobile.feature.expense.data.remote.dto.ExpenseDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ExpenseApi {
    @GET("expenses")
    suspend fun getExpenses(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("category") category: String? = null,
    ): ApiResponse<PaginatedResponse<ExpenseDto>>

    @GET("expenses/{id}")
    suspend fun getExpenseById(
        @Path("id") id: String,
    ): ApiResponse<ExpenseDto>

    @POST("expenses")
    suspend fun createExpense(
        @Body request: CreateExpenseRequest,
    ): ApiResponse<CreateExpenseResponse>

    @Multipart
    @POST("expenses/{id}/bill")
    suspend fun uploadBill(
        @Path("id") id: String,
        @Part bill: MultipartBody.Part,
    ): ApiResponse<UploadResponseDto>
}
