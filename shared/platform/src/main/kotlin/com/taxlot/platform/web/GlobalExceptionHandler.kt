package com.taxlot.platform.web

import com.taxlot.platform.error.*
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    private fun getRequestId() = MDC.get("requestId")

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<ApiError> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError(ex.code, ex.message ?: "Validation failed", getRequestId(), ex.fieldErrors))
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<ApiError> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError(ex.code, ex.message ?: "Not found", getRequestId()))
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(ex: UnauthorizedException): ResponseEntity<ApiError> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError(ex.code, ex.message ?: "Unauthorized", getRequestId()))
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(ex: ForbiddenException): ResponseEntity<ApiError> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError(ex.code, ex.message ?: "Forbidden", getRequestId()))
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflictException(ex: ConflictException): ResponseEntity<ApiError> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError(ex.code, ex.message ?: "Conflict", getRequestId()))
    }

    @ExceptionHandler(BusinessRuleException::class)
    fun handleBusinessRuleException(ex: BusinessRuleException): ResponseEntity<ApiError> {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ApiError(ex.code, ex.message ?: "Business rule violation", getRequestId()))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val fieldErrors = ex.bindingResult.fieldErrors.map { FieldValidationError(it.field, it.defaultMessage ?: "Invalid value") }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError("VALIDATION_ERROR", "Validation failed", getRequestId(), fieldErrors))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ApiError> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError("MALFORMED_REQUEST", "Malformed JSON request", getRequestId()))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ApiError> {
        log.error("Unhandled exception", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError("INTERNAL_ERROR", "An unexpected error occurred", getRequestId()))
    }
}
