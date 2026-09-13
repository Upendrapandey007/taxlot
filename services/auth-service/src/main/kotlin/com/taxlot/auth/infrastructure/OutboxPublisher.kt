package com.taxlot.auth.infrastructure

import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OutboxPublisher(
    private val outboxRepository: OutboxRepository,
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${taxlot.rabbitmq.exchange:taxlot.events}") private val exchange: String
) {
    private val log = LoggerFactory.getLogger(OutboxPublisher::class.java)

    @Scheduled(fixedDelayString = "\${taxlot.outbox.poll-interval-ms:5000}")
    fun publishPendingEvents() {
        val pending = outboxRepository.findUnpublished(10)
        
        for (record in pending) {
            try {
                rabbitTemplate.convertAndSend(exchange, record.routingKey, record.payload)
                outboxRepository.markPublished(record.id!!)
                log.debug("Successfully published event {} with routing key {}", record.eventId, record.routingKey)
            } catch (e: Exception) {
                log.error("Failed to publish event {}", record.eventId, e)
                outboxRepository.incrementAttempt(record.id!!, e.message ?: "Unknown error")
            }
        }
    }
}
