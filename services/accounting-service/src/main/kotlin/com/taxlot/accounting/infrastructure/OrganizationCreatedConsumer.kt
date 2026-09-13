package com.taxlot.accounting.infrastructure

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.accounting.application.InitializeDefaultAccountsUseCase
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OrganizationCreatedConsumer(
    private val objectMapper: ObjectMapper,
    private val initializeDefaultAccountsUseCase: InitializeDefaultAccountsUseCase,
    private val accountRepository: AccountRepository
) {
    private val log = LoggerFactory.getLogger(OrganizationCreatedConsumer::class.java)

    @RabbitListener(queues = ["accounting-service.organization-created"])
    fun consume(message: String) {
        log.info("Received OrganizationCreated event: $message")
        try {
            val root: JsonNode = objectMapper.readTree(message)
            val organizationIdStr = root.path("organizationId").asText()
            if (organizationIdStr.isNotBlank()) {
                val organizationId = UUID.fromString(organizationIdStr)
                val existingAccounts = accountRepository.listByOrganization(organizationId)
                if (existingAccounts.isEmpty()) {
                    initializeDefaultAccountsUseCase.execute(organizationId)
                }
            }
        } catch (e: Exception) {
            log.error("Failed to process OrganizationCreated event", e)
            throw e
        }
    }
}
