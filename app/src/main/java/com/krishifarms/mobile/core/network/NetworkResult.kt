package com.krishifarms.mobile.core.network

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>
}

suspend fun <T> safeApiCall(block: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(block())
    } catch (exception: retrofit2.HttpException) {
        val message = exception.response()?.errorBody()?.string()?.let { body ->
            runCatching {
                kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                }.decodeFromString<ApiErrorResponse>(body).error?.message
            }.getOrNull()
        } ?: exception.message().orEmpty()
        NetworkResult.Error(message.ifBlank { "Request failed" }, exception.code())
    } catch (exception: Exception) {
        NetworkResult.Error(exception.message ?: "Unknown error")
    }
}
