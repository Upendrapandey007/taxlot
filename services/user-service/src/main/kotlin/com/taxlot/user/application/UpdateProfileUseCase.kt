package com.taxlot.user.application

import com.taxlot.platform.error.NotFoundException
import com.taxlot.user.infrastructure.UserProfileRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UpdateProfileUseCase(
    private val repository: UserProfileRepository
) {
    fun execute(userId: UUID, command: UpdateProfileCommand): UserProfileResponse {
        val profile = repository.findByUserId(userId)
            ?: throw NotFoundException("USER_NOT_FOUND", "User profile not found")
        
        val updated = profile.copy(
            fullName = command.fullName ?: profile.fullName,
            avatarUrl = if (command.avatarUrl != null) command.avatarUrl else profile.avatarUrl,
            locale = command.locale ?: profile.locale,
            timezone = command.timezone ?: profile.timezone
        )
        
        val saved = repository.update(updated)
        
        return UserProfileResponse(
            id = saved.id,
            userId = saved.userId,
            email = saved.email,
            fullName = saved.fullName,
            avatarUrl = saved.avatarUrl,
            locale = saved.locale,
            timezone = saved.timezone,
            isActive = saved.isActive
        )
    }
}

data class UpdateProfileCommand(
    val fullName: String?,
    val avatarUrl: String?,
    val locale: String?,
    val timezone: String?
)
