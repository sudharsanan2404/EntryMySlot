package com.entrymyslotbe.app.core

sealed interface ApiResult<out T> {
    data class Success<T>(
        val value: T,
        val httpCode: Int,
        val message: String? = null,
    ) : ApiResult<T>

    sealed interface Failure : ApiResult<Nothing> {
        val userMessage: String
    }

    data class NoInternet(
        override val userMessage: String = "No internet connection. Check your network and try again.",
    ) : Failure

    data class Unauthorized(
        override val userMessage: String = "Your session expired. Please log in again.",
    ) : Failure

    data class HttpError(
        val httpCode: Int,
        override val userMessage: String,
        val serverError: String? = null,
    ) : Failure

    data class NetworkError(
        override val userMessage: String,
        val cause: Throwable? = null,
    ) : Failure

    data class UnexpectedError(
        override val userMessage: String = "Something went wrong. Please try again.",
        val cause: Throwable? = null,
    ) : Failure
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(value), httpCode, message)
    is ApiResult.Failure -> this
}

fun ApiResult<*>.messageForUi(): String = when (this) {
    is ApiResult.Success -> message ?: "Success (${httpCode})"
    is ApiResult.Failure -> userMessage
}
