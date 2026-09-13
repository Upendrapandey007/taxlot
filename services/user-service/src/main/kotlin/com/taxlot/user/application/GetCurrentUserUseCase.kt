package com.taxlot.user.application

import com.taxlot.platform.error.NotFoundException
import com.taxlot.user.domain.UserProfile
import com.taxlot.user.infrastructure.UserProfileRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetCurrentUserUseCase(
    private val repository: UserProfileRepository
) {
    fun execute(userId: UUID): UserProfileResponse {
        val profile = repository.findByUserId(userId)
            ?: throw NotFoundException("USER_NOT_FOUND", "User profile not found")
        
        return UserProfileResponse(
            id = profile.id,
            userId = profile.userId,
            email = profile.email,
            fullName = profile.fullName,
            avatarUrl = profile.avatarUrl,
            locale = profile.locale,
            timezone = profile.timezone,
            isActive = profile.isActive
        )
    }
}

data class UserProfileResponse(
    val id: UUID,
    val userId: UUID,
    val email: String,
    val fullName: String,
    val avatarUrl: String?,
    val locale: String,
    val timezone: String,
    val isActive: Boolean
)
