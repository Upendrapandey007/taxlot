package com.taxlot.modules.users

import com.taxlot.common.exception.NotFoundException
import com.taxlot.modules.users.dto.UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User endpoints")
class UsersController(
    private val userRepository: UserRepository
) {

    @GetMapping("/me")
    @Operation(summary = "Get current logged in user")
    fun getMe(@AuthenticationPrincipal userId: String): UserResponse {
        val user = userRepository.findById(UUID.fromString(userId))
            ?: throw NotFoundException("User not found")
            
        return UserResponse(
            id = user.id,
            email = user.email,
            fullName = user.fullName,
            createdAt = user.createdAt
        )
    }
}
