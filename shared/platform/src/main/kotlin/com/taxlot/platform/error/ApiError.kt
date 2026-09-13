package com.taxlot.platform.error

data class ApiError(
    val code: String,
    val message: String,
    val requestId: String? = null,
    val details: List<FieldValidationError>? = null
)

data class FieldValidationError(val field: String, val message: String)
