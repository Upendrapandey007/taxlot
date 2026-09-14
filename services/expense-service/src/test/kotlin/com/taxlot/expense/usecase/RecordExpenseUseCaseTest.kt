package com.taxlot.expense.usecase

import com.taxlot.expense.domain.*
import com.taxlot.expense.repository.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import org.mockito.Mockito.*

class RecordExpenseUseCaseTest : StringSpec({
    val expenseRepo = mock(ExpenseRepository::class.java)
    val outboxRepo = mock(ExpenseOutboxRepository::class.java)
    val useCase = RecordExpenseUseCase(expenseRepo, outboxRepo)

    "should record valid expense" {
        val orgId = UUID.randomUUID()
        val expense = Expense(
            organizationId = orgId,
            expenseNumber = "",
            vendorName = "Test Vendor",
            category = ExpenseCategory.OFFICE_SUPPLIES,
            expenseDate = LocalDate.now(),
            subtotal = BigDecimal("100.00"),
            taxAmount = BigDecimal("10.00"),
            totalAmount = BigDecimal("110.00")
        )
        
        `when`(expenseRepo.generateExpenseNumber(anyInt())).thenReturn("EXP-2024-0001")
        `when`(expenseRepo.save(any())).thenAnswer { it.arguments[0] as Expense }
        
        val result = useCase.execute(RecordExpenseCommand(expense))
        
        result.expenseNumber shouldBe "EXP-2024-0001"
        result.totalAmount shouldBe BigDecimal("110.00")
        verify(outboxRepo, times(1)).save(any(), anyString(), anyString(), any(), any(), anyString(), any())
    }

    "should throw exception if amounts do not match" {
        val orgId = UUID.randomUUID()
        shouldThrow<IllegalArgumentException> {
            Expense(
                organizationId = orgId,
                expenseNumber = "",
                vendorName = "Test Vendor",
                category = ExpenseCategory.OFFICE_SUPPLIES,
                expenseDate = LocalDate.now(),
                subtotal = BigDecimal("100.00"),
                taxAmount = BigDecimal("10.00"),
                totalAmount = BigDecimal("100.00") // invalid
            )
        }
    }
})
