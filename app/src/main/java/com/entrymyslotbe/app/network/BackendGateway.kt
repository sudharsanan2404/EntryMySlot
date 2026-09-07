package com.entrymyslotbe.app.network

import com.entrymyslotbe.app.core.ApiResult
import com.entrymyslotbe.app.network.model.ApiError
import com.entrymyslotbe.app.network.model.ApiResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class BackendGateway(
    val api: ApiService,
    private val connectivity: ConnectivityMonitor,
    private val gson: Gson,
) {
    suspend fun <T> execute(call: suspend ApiService.() -> Response<T>): ApiResult<T> =
        withContext(Dispatchers.IO) {
            if (!connectivity.isOnline()) return@withContext ApiResult.NoInternet()

            try {
                val response = api.call()
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    when {
                        body is ApiResponse<*> && !body.success -> ApiResult.HttpError(response.code(), body.message ?: "The server rejected the request.")
                        body is com.entrymyslotbe.app.network.model.PaginatedResponse<*> && !body.success -> ApiResult.HttpError(response.code(), "The server rejected the request.")
                        else -> ApiResult.Success(body, response.code(), response.message())
                    }
                } else if (response.code() == 401) {
                    ApiResult.Unauthorized()
                } else {
                    val parsed = response.errorBody()?.string()?.let {
                        runCatching { gson.fromJson(it, ApiError::class.java) }.getOrNull()
                    }
                    ApiResult.HttpError(
                        httpCode = response.code(),
                        userMessage = parsed?.message ?: parsed?.error
                            ?: "Request failed (${response.code()}).",
                        serverError = parsed?.error,
                    )
                }
            } catch (error: kotlinx.coroutines.CancellationException) {
                throw error
            } catch (error: UnknownHostException) {
                ApiResult.NoInternet()
            } catch (error: SocketTimeoutException) {
                ApiResult.NetworkError("The server took too long to respond.", error)
            } catch (error: IOException) {
                ApiResult.NetworkError("Could not reach the server. Please try again.", error)
            } catch (error: Throwable) {
                ApiResult.UnexpectedError(cause = error)
            }
        }

    suspend fun <T> data(call: suspend ApiService.() -> Response<ApiResponse<T>>): ApiResult<T?> =
        when (val result = execute(call)) {
            is ApiResult.Success -> {
                val envelope = result.value
                if (envelope.success) {
                    ApiResult.Success(envelope.data, result.httpCode, envelope.message)
                } else {
                    ApiResult.HttpError(
                        result.httpCode,
                        envelope.message ?: "The server rejected the request.",
                    )
                }
            }
            is ApiResult.Failure -> result
        }
}
