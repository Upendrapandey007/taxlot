package com.taxlot.user.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.platform.event.EventEnvelope
import com.taxlot.user.domain.UserPreferences
import com.taxlot.user.domain.UserProfile
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class UserRegisteredEventConsumer(
    private val userProfileRepository: UserProfileRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(UserRegisteredEventConsumer::class.java)

    @Transactional
    @RabbitListener(queues = ["user-service.user-registered"])
    fun handleUserRegistered(message: Message) {
        val body = String(message.body)
        log.info("Received user registered event: {}", body)
        
        val envelope = objectMapper.readValue(body, EventEnvelope::class.java)
        
        val payloadStr = objectMapper.writeValueAsString(envelope.payload)
        val payloadMap = objectMapper.readValue(payloadStr, Map::class.java)
        
        val userId = UUID.fromString(payloadMap["userId"] as String)
        val email = payloadMap["email"] as String
        val fullName = payloadMap["fullName"] as String? ?: "Unknown"
        
        if (userProfileRepository.existsByUserId(userId)) {
            log.info("User profile already exists for userId: {}, skipping event", userId)
            return
        }
        
        val profile = UserProfile(userId = userId, email = email, fullName = fullName)
        userProfileRepository.save(profile)
        
        val prefs = UserPreferences(userId = userId)
        userPreferencesRepository.save(prefs)
        
        log.info("Successfully created user profile and preferences for userId: {}", userId)
    }
}
