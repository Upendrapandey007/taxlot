package com.taxlot.common.exception

import com.taxlot.common.model.FieldError

sealed class TaxlotException(message: String) : RuntimeException(message)

class ValidationException(message: String, val fieldErrors: List<FieldError>? = null) : TaxlotException(message)
class NotFoundException(message: String) : TaxlotException(message)
class UnauthorizedException(message: String) : TaxlotException(message)
class ForbiddenException(message: String) : TaxlotException(message)
class ConflictException(message: String) : TaxlotException(message)
class BusinessRuleException(message: String) : TaxlotException(message)
