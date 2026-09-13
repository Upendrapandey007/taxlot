package com.taxlot.auth.domain

enum class SecurityEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    PASSWORD_CHANGED,
    PASSWORD_RESET_REQUESTED,
    PASSWORD_RESET_COMPLETED,
    SESSION_REVOKED,
    EMAIL_VERIFIED,
    LOGOUT
}
