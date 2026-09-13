package com.taxlot.auth.application

import com.taxlot.auth.api.dto.LogoutRequest
import com.taxlot.auth.domain.SecurityEventType
import com.taxlot.auth.infrastructure.RefreshTokenRepository
import com.taxlot.auth.infrastructure.SecurityEventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class LogoutUseCase(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val securityEventRepository: SecurityEventRepository,
    private val tokenService: TokenService
) {
    @Transactional
    fun execute(request: LogoutRequest, userId: UUID, ipAddress: String?, userAgent: String?) {
        val hash = tokenService.hashToken(request.refreshToken)
        refreshTokenRepository.revokeByTokenHash(hash)
        
        securityEventRepository.record(
            credentialId = null, // Can map to credential if we resolve it
            eventType = SecurityEventType.LOGOUT,
            ipAddress = ipAddress,
            userAgent = userAgent,
            metadata = mapOf("userId" to userId.toString())
        )
    }
}
