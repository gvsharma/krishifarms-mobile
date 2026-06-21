package com.krishifarms.mobile.core.sync.handler

import com.krishifarms.mobile.core.sync.domain.RemoteEntitySnapshot
import com.krishifarms.mobile.core.sync.domain.SyncHandlerResult
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class BaseSyncHandler(
    protected val json: Json,
) {
    protected suspend fun <T> executeApi(block: suspend () -> T): SyncHandlerResult {
        return try {
            val result = block()
            onSuccess(result)
        } catch (exception: HttpException) {
            mapHttpException(exception)
        } catch (exception: IOException) {
            SyncHandlerResult.RetryableFailure(exception.message ?: "Network error")
        } catch (exception: Exception) {
            SyncHandlerResult.PermanentFailure(exception.message ?: "Unexpected error")
        }
    }

    protected open fun <T> onSuccess(result: T): SyncHandlerResult =
        SyncHandlerResult.Success()

    protected fun mapHttpException(exception: HttpException): SyncHandlerResult {
        val code = exception.code()
        val message = exception.message() ?: "HTTP $code"
        return when (code) {
            409 -> SyncHandlerResult.Conflict(
                serverSnapshot = RemoteEntitySnapshot(
                    entityId = "",
                    updatedAt = System.currentTimeMillis(),
                    payloadJson = "{}",
                ),
                clientUpdatedAt = System.currentTimeMillis(),
            )

            in 400..499 -> SyncHandlerResult.PermanentFailure(message)
            in 500..599 -> SyncHandlerResult.RetryableFailure(message)
            else -> SyncHandlerResult.RetryableFailure(message)
        }
    }

    protected fun isRetryableNetworkError(exception: Throwable): Boolean =
        exception is UnknownHostException ||
            exception is SocketTimeoutException ||
            exception is IOException
}
