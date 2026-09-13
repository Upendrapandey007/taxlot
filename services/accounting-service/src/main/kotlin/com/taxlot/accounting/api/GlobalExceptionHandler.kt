package com.taxlot.accounting.api

import com.taxlot.accounting.domain.exceptions.BusinessRuleException
import com.taxlot.accounting.domain.exceptions.ConflictException
import com.taxlot.accounting.domain.exceptions.ValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuleException::class)
    fun handleBusinessRuleException(ex: BusinessRuleException): ResponseEntity<Map<String, String>> {
        val status = when (ex) {
            is ValidationException -> HttpStatus.UNPROCESSABLE_ENTITY
            is ConflictException -> HttpStatus.CONFLICT
            else -> HttpStatus.UNPROCESSABLE_ENTITY
        }
        return ResponseEntity.status(status).body(mapOf(
            "code" to ex.code,
            "message" to (ex.message ?: "Business rule violation")
        ))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
            "code" to "INTERNAL_ERROR",
            "message" to (ex.message ?: "An unexpected error occurred")
        ))
    }
}
