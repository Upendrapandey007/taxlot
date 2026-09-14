package com.taxlot.payment.usecase

import com.taxlot.payment.domain.*
import com.taxlot.payment.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

data class RecordPaymentCommand(
    val payment: Payment,
    val allocations: List<PaymentAllocation> = emptyList()
)

@Service
class RecordPaymentUseCase(
    private val paymentRepository: PaymentRepository,
    private val allocationRepository: PaymentAllocationRepository,
    private val outboxRepository: PaymentOutboxRepository
) {
    @Transactional
    fun execute(command: RecordPaymentCommand): Payment {
        val totalAllocated = command.allocations.sumOf { it.allocatedAmount }
        require(totalAllocated <= command.payment.amount) { "Allocated amount exceeds payment amount" }

        val year = command.payment.paymentDate.year
        val paymentNumber = paymentRepository.generatePaymentNumber(year)
        val paymentToSave = command.payment.copy(paymentNumber = paymentNumber)
        
        val savedPayment = paymentRepository.save(paymentToSave)
        
        val allocationsToSave = command.allocations.map {
            it.copy(paymentId = savedPayment.id, organizationId = savedPayment.organizationId)
        }
        
        if (allocationsToSave.isNotEmpty()) {
            allocationRepository.saveAll(allocationsToSave)
        }
        
        val payload = mapOf(
            "payment" to savedPayment,
            "allocations" to allocationsToSave
        )
        
        outboxRepository.save(
            eventId = UUID.randomUUID(),
            eventType = "PaymentReceived",
            aggregateType = "Payment",
            aggregateId = savedPayment.id,
            tenantId = savedPayment.organizationId,
            routingKey = "payment.payment.received",
            payload = payload
        )
        
        return savedPayment
    }
}

@Service
class RefundPaymentUseCase(
    private val paymentRepository: PaymentRepository,
    private val outboxRepository: PaymentOutboxRepository
) {
    @Transactional
    fun execute(id: UUID, organizationId: UUID) {
        val payment = paymentRepository.findById(id, organizationId) ?: throw RuntimeException("Not found")
        paymentRepository.updateStatus(id, organizationId, PaymentStatus.REFUNDED)
        
        outboxRepository.save(
            eventId = UUID.randomUUID(),
            eventType = "PaymentRefunded",
            aggregateType = "Payment",
            aggregateId = id,
            tenantId = organizationId,
            routingKey = "payment.payment.refunded",
            payload = mapOf("id" to id, "status" to "REFUNDED")
        )
    }
}

@Service
class GetPaymentUseCase(private val paymentRepository: PaymentRepository) {
    fun execute(id: UUID, organizationId: UUID): Payment? {
        return paymentRepository.findById(id, organizationId)
    }
}

@Service
class ListPaymentsUseCase(private val paymentRepository: PaymentRepository) {
    fun execute(organizationId: UUID, page: Int, size: Int): List<Payment> {
        return paymentRepository.findAll(organizationId, page, size)
    }
}
