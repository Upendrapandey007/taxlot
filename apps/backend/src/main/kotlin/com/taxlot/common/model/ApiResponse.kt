package com.taxlot.common.model

data class ApiResponse<T>(
    val data: T,
    val requestId: String? = null
)
