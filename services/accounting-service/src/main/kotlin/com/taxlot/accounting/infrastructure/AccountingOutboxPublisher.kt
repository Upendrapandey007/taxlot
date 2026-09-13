package com.taxlot.accounting.infrastructure

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class AccountingOutboxPublisher(
    private val outboxRepository: AccountingOutboxRepository,
    private val rabbitTemplate: RabbitTemplate
) {

    @Scheduled(fixedDelayString = "\${taxlot.outbox.poll-interval-ms:5000}")
    fun publishEvents() {
        val records = outboxRepository.findUnpublished(20)
        for (record in records) {
            try {
                rabbitTemplate.convertAndSend("taxlot.events", record.routingKey, record.payload)
                outboxRepository.markPublished(record.id)
            } catch (e: Exception) {
                outboxRepository.incrementAttempt(record.id, e.message ?: "Unknown error")
            }
        }
    }
}
