package com.taxlot.invoice.infrastructure.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.invoice.infrastructure.repository.InvoiceOutboxRepository
import com.taxlot.invoice.application.usecase.InvoiceUseCases
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID
import org.slf4j.LoggerFactory

data class PaymentAllocation(val invoiceId: UUID, val allocatedAmount: BigDecimal)
data class EventEnvelope(val eventId: UUID, val eventType: String, val tenantId: UUID?, val payload: Any)

@Component
class PaymentReceivedConsumer(
    private val invoiceUseCases: InvoiceUseCases,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(PaymentReceivedConsumer::class.java)

    @RabbitListener(queues = ["invoice-service.payment-received"])
    fun onPaymentReceived(message: String) {
        try {
            val rootNode = objectMapper.readTree(message)
            val tenantIdStr = rootNode.get("tenantId")?.asText()
            if (tenantIdStr == null) {
                logger.warn("Received payment event without tenantId")
                return
            }
            val tenantId = UUID.fromString(tenantIdStr)
            val payloadNode = rootNode.get("payload")
            val allocationsNode = payloadNode.get("allocations")
            
            if (allocationsNode != null && allocationsNode.isArray) {
                for (allocNode in allocationsNode) {
                    val invoiceId = UUID.fromString(allocNode.get("invoiceId").asText())
                    val allocatedAmount = BigDecimal(allocNode.get("allocatedAmount").asText())
                    invoiceUseCases.applyPayment(invoiceId, tenantId, allocatedAmount)
                }
            }
        } catch (e: Exception) {
            logger.error("Error processing payment received event", e)
            throw e
        }
    }
}

@Component
class InvoiceOutboxPublisher(
    private val outboxRepository: InvoiceOutboxRepository,
    private val rabbitTemplate: RabbitTemplate
) {
    private val logger = LoggerFactory.getLogger(InvoiceOutboxPublisher::class.java)

    @Scheduled(fixedDelay = 5000)
    fun publishEvents() {
        val events = outboxRepository.findUnpublished(100)
        for (event in events) {
            try {
                rabbitTemplate.convertAndSend("taxlot.events", event.routingKey, event.payload)
                val updatedEvent = event.copy(publishedAt = OffsetDateTime.now())
                outboxRepository.save(updatedEvent)
            } catch (e: Exception) {
                logger.error("Failed to publish event ${event.id}", e)
                val updatedEvent = event.copy(
                    attemptCount = event.attemptCount + 1,
                    lastError = e.message
                )
                outboxRepository.save(updatedEvent)
            }
        }
    }
}
