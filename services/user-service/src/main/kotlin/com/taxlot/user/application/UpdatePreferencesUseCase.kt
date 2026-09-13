package com.taxlot.user.application

import com.taxlot.platform.error.NotFoundException
import com.taxlot.user.infrastructure.UserPreferencesRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UpdatePreferencesUseCase(
    private val repository: UserPreferencesRepository
) {
    fun execute(userId: UUID, command: UpdatePreferencesCommand): UserPreferencesResponse {
        val prefs = repository.findByUserId(userId)
            ?: throw NotFoundException("PREFERENCES_NOT_FOUND", "User preferences not found")
        
        val updated = prefs.copy(
            emailNotifications = command.emailNotifications ?: prefs.emailNotifications,
            pushNotifications = command.pushNotifications ?: prefs.pushNotifications,
            currencyDisplay = command.currencyDisplay ?: prefs.currencyDisplay,
            dateFormat = command.dateFormat ?: prefs.dateFormat
        )
        
        val saved = repository.update(updated)
        
        return UserPreferencesResponse(
            emailNotifications = saved.emailNotifications,
            pushNotifications = saved.pushNotifications,
            currencyDisplay = saved.currencyDisplay,
            dateFormat = saved.dateFormat
        )
    }
}

data class UpdatePreferencesCommand(
    val emailNotifications: Boolean?,
    val pushNotifications: Boolean?,
    val currencyDisplay: String?,
    val dateFormat: String?
)
