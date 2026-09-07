package com.entrymyslotbe.app.network.model

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val expiresInMinutes: Int? = null,
)

data class ApiError(
    val success: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

data class PaginatedResponse<T>(
    val success: Boolean,
    val data: List<T>? = null,
    val pagination: Pagination? = null
)

data class Pagination(
    val page: Int? = null,
    val pageSize: Int? = null,
    @com.google.gson.annotations.SerializedName(value = "totalItems", alternate = ["total"])
    val totalItems: Int? = null,
    val totalPages: Int? = null
)
