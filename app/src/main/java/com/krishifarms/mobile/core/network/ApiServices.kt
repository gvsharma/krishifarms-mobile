package com.krishifarms.mobile.core.network

import com.krishifarms.mobile.core.network.dto.AuthDtos
import com.krishifarms.mobile.core.network.dto.DocumentDtos
import com.krishifarms.mobile.core.network.dto.ExpenseDtos
import com.krishifarms.mobile.core.network.dto.FarmDtos
import com.krishifarms.mobile.core.network.dto.FarmerDtos
import com.krishifarms.mobile.core.network.dto.PaymentDtos
import com.krishifarms.mobile.core.network.dto.ProcurementDtos
import com.krishifarms.mobile.core.network.dto.SyncDtos
import com.krishifarms.mobile.core.network.dto.WorkerDtos
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApiService {
    @POST("auth/otp/send")
    suspend fun sendOtp(@Body request: AuthDtos.SendOtpRequest): AuthDtos.OtpResponse

    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body request: AuthDtos.VerifyOtpRequest): AuthDtos.AuthResponse

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: AuthDtos.RefreshTokenRequest): AuthDtos.AuthResponse

    @GET("auth/me")
    suspend fun getCurrentUser(): AuthDtos.UserDto
}

interface FarmerApiService {
    @GET("farmers")
    suspend fun getFarmers(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50,
        @Query("since") since: Long? = null,
    ): FarmerDtos.FarmerListResponse

    @GET("farmers/search")
    suspend fun searchFarmers(
        @Query("q") query: String,
    ): FarmerDtos.FarmerListResponse

    @GET("farmers/{id}")
    suspend fun getFarmer(@Path("id") id: String): FarmerDtos.FarmerDto

    @POST("farmers")
    suspend fun createFarmer(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: FarmerDtos.CreateFarmerRequest,
    ): FarmerDtos.FarmerDto

    @PUT("farmers/{id}")
    suspend fun updateFarmer(
        @Path("id") id: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: FarmerDtos.UpdateFarmerRequest,
    ): FarmerDtos.FarmerDto
}

interface FarmApiService {
    @GET("farms")
    suspend fun getFarms(
        @Query("page") page: Int = 1,
        @Query("since") since: Long? = null,
    ): FarmDtos.FarmListResponse
}

interface ProcurementApiService {
    @GET("procurements")
    suspend fun getProcurements(
        @Query("page") page: Int = 1,
        @Query("since") since: Long? = null,
    ): ProcurementDtos.ProcurementListResponse

    @POST("procurements")
    suspend fun createProcurement(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: ProcurementDtos.CreateProcurementRequest,
    ): ProcurementDtos.ProcurementDto
}

interface ExpenseApiService {
    @GET("expenses")
    suspend fun getExpenses(
        @Query("page") page: Int = 1,
        @Query("since") since: Long? = null,
    ): ExpenseDtos.ExpenseListResponse

    @GET("expenses/{id}")
    suspend fun getExpense(@Path("id") id: String): ExpenseDtos.ExpenseDto

    @POST("expenses")
    suspend fun createExpense(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: ExpenseDtos.CreateExpenseRequest,
    ): ExpenseDtos.ExpenseDto

    @POST("expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: ExpenseDtos.UpdateExpenseRequest,
    ): ExpenseDtos.ExpenseDto
}

interface PaymentApiService {
    @GET("payments")
    suspend fun getPayments(
        @Query("page") page: Int = 1,
        @Query("since") since: Long? = null,
    ): PaymentDtos.PaymentListResponse
}

interface WorkerApiService {
    @GET("workers")
    suspend fun getWorkers(
        @Query("page") page: Int = 1,
        @Query("since") since: Long? = null,
    ): WorkerDtos.WorkerListResponse

    @GET("workers/{id}")
    suspend fun getWorker(@Path("id") id: String): WorkerDtos.WorkerDto

    @POST("workers")
    suspend fun createWorker(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: WorkerDtos.CreateWorkerRequest,
    ): WorkerDtos.WorkerDto

    @POST("workers/{id}")
    suspend fun updateWorker(
        @Path("id") id: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: WorkerDtos.UpdateWorkerRequest,
    ): WorkerDtos.WorkerDto
}

interface DocumentApiService {
    @Multipart
    @POST("documents/upload")
    suspend fun uploadDocument(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Part("metadata") metadata: RequestBody,
        @Part file: MultipartBody.Part,
    ): DocumentDtos.UploadDocumentResponse

    @GET("documents/{id}")
    suspend fun getDocument(@Path("id") id: String): DocumentDtos.DocumentDto
}

interface WorkOrderApiService {
    @GET("work-orders")
    suspend fun getWorkOrders(
        @Query("page") page: Int = 1,
        @Query("since") since: Long? = null,
        @Query("worker_id") workerId: String? = null,
    ): WorkerDtos.WorkOrderListResponse

    @GET("work-orders/{id}")
    suspend fun getWorkOrder(@Path("id") id: String): WorkerDtos.WorkOrderDto

    @POST("work-orders")
    suspend fun createWorkOrder(@Body request: WorkerDtos.CreateWorkOrderRequest): WorkerDtos.WorkOrderDto
}

interface AttendanceApiService {
    @GET("attendance")
    suspend fun getAttendance(
        @Query("date") date: Long? = null,
        @Query("since") since: Long? = null,
    ): WorkerDtos.AttendanceListResponse

    @POST("attendance")
    suspend fun upsertAttendance(@Body request: WorkerDtos.UpsertAttendanceRequest): WorkerDtos.AttendanceDto
}

interface SyncApiService {
    @POST("sync/push")
    suspend fun pushChanges(@Body request: SyncDtos.PushRequest): SyncDtos.PushResponse

    @GET("sync/pull")
    suspend fun pullChanges(@Query("since") since: Long): SyncDtos.PullResponse
}
