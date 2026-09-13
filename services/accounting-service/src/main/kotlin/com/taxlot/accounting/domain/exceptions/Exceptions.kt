package com.taxlot.accounting.domain.exceptions

open class BusinessRuleException(val code: String, message: String) : RuntimeException(message)
class ValidationException(code: String, message: String) : BusinessRuleException(code, message)
class ConflictException(code: String, message: String) : BusinessRuleException(code, message)