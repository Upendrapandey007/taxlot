package com.taxlot.common.model

data class ApiError(
    val code: String,
    val message: String,
    val requestId: String?,
    val details: List<FieldError>? = null
)
