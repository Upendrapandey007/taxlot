package com.taxlot.payment.api

import com.taxlot.payment.domain.*
import com.taxlot.payment.usecase.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/payments")
class PaymentsController(
    private val recordPaymentUseCase: RecordPaymentUseCase,
    private val refundPaymentUseCase: RefundPaymentUseCase,
    private val getPaymentUseCase: GetPaymentUseCase,
    private val listPaymentsUseCase: ListPaymentsUseCase
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun recordPayment(
        @RequestHeader("X-Tenant-Id") tenantId: UUID,
        @RequestBody request: RecordPaymentCommand
    ): Payment {
        val payment = request.payment.copy(organizationId = tenantId, id = UUID.randomUUID())
        return recordPaymentUseCase.execute(RecordPaymentCommand(payment, request.allocations))
    }

    @GetMapping("/{id}")
    fun getPayment(
        @RequestHeader("X-Tenant-Id") tenantId: UUID,
        @PathVariable id: UUID
    ): Payment {
        return getPaymentUseCase.execute(id, tenantId) ?: throw RuntimeException("Not found")
    }

    @GetMapping
    fun listPayments(
        @RequestHeader("X-Tenant-Id") tenantId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): List<Payment> {
        return listPaymentsUseCase.execute(tenantId, page, size)
    }

    @PostMapping("/{id}/refund")
    fun refundPayment(
        @RequestHeader("X-Tenant-Id") tenantId: UUID,
        @PathVariable id: UUID
    ) {
        refundPaymentUseCase.execute(id, tenantId)
    }
}
