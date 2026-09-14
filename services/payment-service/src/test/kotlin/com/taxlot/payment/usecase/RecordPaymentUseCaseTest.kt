package com.taxlot.payment.usecase

import com.taxlot.payment.domain.*
import com.taxlot.payment.repository.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import org.mockito.Mockito.*

class RecordPaymentUseCaseTest : StringSpec({
    val paymentRepo = mock(PaymentRepository::class.java)
    val allocRepo = mock(PaymentAllocationRepository::class.java)
    val outboxRepo = mock(PaymentOutboxRepository::class.java)
    val useCase = RecordPaymentUseCase(paymentRepo, allocRepo, outboxRepo)

    "should record valid payment with allocations" {
        val orgId = UUID.randomUUID()
        val payment = Payment(
            organizationId = orgId,
            paymentNumber = "",
            customerId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            paymentDate = LocalDate.now()
        )
        val alloc = PaymentAllocation(
            paymentId = payment.id,
            organizationId = orgId,
            invoiceId = UUID.randomUUID(),
            allocatedAmount = BigDecimal("100.00")
        )
        
        `when`(paymentRepo.generatePaymentNumber(anyInt())).thenReturn("PAY-2024-0001")
        `when`(paymentRepo.save(any())).thenAnswer { it.arguments[0] as Payment }
        
        val result = useCase.execute(RecordPaymentCommand(payment, listOf(alloc)))
        
        result.paymentNumber shouldBe "PAY-2024-0001"
        result.amount shouldBe BigDecimal("100.00")
        verify(outboxRepo, times(1)).save(any(), anyString(), anyString(), any(), any(), anyString(), any())
        verify(allocRepo, times(1)).saveAll(anyList())
    }

    "should throw exception if overallocated" {
        val orgId = UUID.randomUUID()
        val payment = Payment(
            organizationId = orgId,
            paymentNumber = "",
            customerId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            paymentDate = LocalDate.now()
        )
        val alloc = PaymentAllocation(
            paymentId = payment.id,
            organizationId = orgId,
            invoiceId = UUID.randomUUID(),
            allocatedAmount = BigDecimal("150.00")
        )
        
        shouldThrow<IllegalArgumentException> {
            useCase.execute(RecordPaymentCommand(payment, listOf(alloc)))
        }
    }
})
