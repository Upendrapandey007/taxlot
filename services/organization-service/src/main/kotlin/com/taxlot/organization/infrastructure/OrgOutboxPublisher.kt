package com.taxlot.organization.infrastructure

import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OrgOutboxPublisher(
    private val outboxRepository: OrgOutboxRepository,
    private val rabbitTemplate: RabbitTemplate
) {
    private val log = LoggerFactory.getLogger(OrgOutboxPublisher::class.java)

    @Scheduled(fixedDelayString = "\${taxlot.outbox.delay:2000}")
    fun publishEvents() {
        val events = outboxRepository.findUnpublished(100)
        if (events.isEmpty()) return

        for (event in events) {
            try {
                // Determine exchange from routing key (e.g., "organization.organization.created")
                val parts = event.routingKey.split(".")
                val exchange = if (parts.isNotEmpty()) parts[0] + "-exchange" else "amq.topic"
                
                rabbitTemplate.convertAndSend(exchange, event.routingKey, event.payload)
                outboxRepository.markPublished(event.id)
                log.debug("Successfully published event {} to exchange {}", event.eventId, exchange)
            } catch (ex: Exception) {
                log.error("Failed to publish event {}", event.eventId, ex)
                outboxRepository.incrementAttempt(event.id, ex.message ?: "Unknown error")
            }
        }
    }
}
