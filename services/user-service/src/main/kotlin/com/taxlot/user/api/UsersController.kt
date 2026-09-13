package com.taxlot.user.api

import com.taxlot.user.application.*
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
class UsersController(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val getPreferencesUseCase: GetPreferencesUseCase,
    private val updatePreferencesUseCase: UpdatePreferencesUseCase
) {

    @GetMapping("/me")
    fun getMe(@RequestHeader("X-User-Id") userId: UUID): UserProfileResponse {
        return getCurrentUserUseCase.execute(userId)
    }

    @PatchMapping("/me")
    fun updateMe(
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestBody request: UpdateProfileCommand
    ): UserProfileResponse {
        return updateProfileUseCase.execute(userId, request)
    }

    @GetMapping("/me/preferences")
    fun getMyPreferences(@RequestHeader("X-User-Id") userId: UUID): UserPreferencesResponse {
        return getPreferencesUseCase.execute(userId)
    }

    @PatchMapping("/me/preferences")
    fun updateMyPreferences(
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestBody request: UpdatePreferencesCommand
    ): UserPreferencesResponse {
        return updatePreferencesUseCase.execute(userId, request)
    }
}
