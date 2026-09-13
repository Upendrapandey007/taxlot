package com.taxlot.common.exception

import com.taxlot.common.model.ApiError
import com.taxlot.common.model.FieldError
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
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(TaxlotException::class)
    fun handleTaxlotException(ex: TaxlotException): ResponseEntity<ApiError> {
        val status = when (ex) {
            is ValidationException -> HttpStatus.BAD_REQUEST
            is NotFoundException -> HttpStatus.NOT_FOUND
            is UnauthorizedException -> HttpStatus.UNAUTHORIZED
            is ForbiddenException -> HttpStatus.FORBIDDEN
            is ConflictException -> HttpStatus.CONFLICT
            is BusinessRuleException -> HttpStatus.UNPROCESSABLE_ENTITY
        }
        val details = if (ex is ValidationException) ex.fieldErrors else null
        return buildResponse(status, status.reasonPhrase, ex.message ?: "Error", details)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val errors = ex.bindingResult.fieldErrors.map {
            FieldError(it.field, it.defaultMessage ?: "Invalid value")
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Invalid request parameters", errors)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<ApiError> {
        return buildResponse(HttpStatus.BAD_REQUEST, "MALFORMED_JSON", "Malformed JSON request")
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiError> {
        log.error("Unhandled exception", ex)
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred")
    }

    private fun buildResponse(
        status: HttpStatus,
        code: String,
        message: String,
        details: List<FieldError>? = null
    ): ResponseEntity<ApiError> {
        val requestId = MDC.get("requestId")
        val error = ApiError(code, message, requestId, details)
        return ResponseEntity.status(status)
            .header("X-Request-Id", requestId)
            .body(error)
    }
}
