package com.entrymyslot.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val pagination: PaginationInfo? = null
)

@Serializable
data class PaginationInfo(
    val page: Int? = null,
    
    @SerialName("pageSize")
    val limit: Int? = null,
    
    val total: Int? = null,
    
    @SerialName("totalPages")
    val totalPages: Int? = null
)

@Serializable
data class ApiMessageResponse(
    val success: Boolean,
    val message: String
)
