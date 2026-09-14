package com.taxlot.expense.outbox

import com.taxlot.expense.repository.ExpenseOutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ExpenseOutboxPublisher(
    private val repository: ExpenseOutboxRepository,
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${taxlot.events.exchange:taxlot.events}") private val exchange: String
) {
    private val logger = LoggerFactory.getLogger(ExpenseOutboxPublisher::class.java)

    @Scheduled(fixedDelay = 5000)
    fun publishEvents() {
        val events = repository.findUnpublished()
        for (event in events) {
            val id = event["id"] as UUID
            val routingKey = event["routing_key"] as String
            val payload = event["payload"] as String
            try {
                rabbitTemplate.convertAndSend(exchange, routingKey, payload)
                repository.markPublished(id)
                logger.info("Published event {}", id)
            } catch (e: Exception) {
                logger.error("Failed to publish event {}", id, e)
            }
        }
    }
}
