package com.taxlot.user.application

import com.taxlot.platform.error.NotFoundException
import com.taxlot.user.infrastructure.UserPreferencesRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetPreferencesUseCase(
    private val repository: UserPreferencesRepository
) {
    fun execute(userId: UUID): UserPreferencesResponse {
        val prefs = repository.findByUserId(userId)
            ?: throw NotFoundException("PREFERENCES_NOT_FOUND", "User preferences not found")
        
        return UserPreferencesResponse(
            emailNotifications = prefs.emailNotifications,
            pushNotifications = prefs.pushNotifications,
            currencyDisplay = prefs.currencyDisplay,
            dateFormat = prefs.dateFormat
        )
    }
}

data class UserPreferencesResponse(
    val emailNotifications: Boolean,
    val pushNotifications: Boolean,
    val currencyDisplay: String,
    val dateFormat: String
)
