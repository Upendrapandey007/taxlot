package com.taxlot.platform.error

sealed class ApiException(val code: String, message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class ValidationException(code: String = "VALIDATION_ERROR", message: String, val fieldErrors: List<FieldValidationError> = emptyList()) : ApiException(code, message)
class NotFoundException(code: String = "NOT_FOUND", message: String) : ApiException(code, message)
class UnauthorizedException(code: String = "UNAUTHORIZED", message: String) : ApiException(code, message)
class ForbiddenException(code: String = "FORBIDDEN", message: String) : ApiException(code, message)
class ConflictException(code: String = "CONFLICT", message: String) : ApiException(code, message)
class BusinessRuleException(code: String = "BUSINESS_RULE_VIOLATION", message: String) : ApiException(code, message)
