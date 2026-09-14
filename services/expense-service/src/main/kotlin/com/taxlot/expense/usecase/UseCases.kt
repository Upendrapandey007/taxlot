package com.taxlot.expense.usecase

import com.taxlot.expense.domain.*
import com.taxlot.expense.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RecordExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
    private val outboxRepository: ExpenseOutboxRepository
) {
    @Transactional
    fun execute(command: RecordExpenseCommand): Expense {
        val year = command.expense.expenseDate.year
        val expenseNumber = expenseRepository.generateExpenseNumber(year)
        
        val expenseToSave = command.expense.copy(expenseNumber = expenseNumber)
        val savedExpense = expenseRepository.save(expenseToSave)
        
        outboxRepository.save(
            eventId = UUID.randomUUID(),
            eventType = "ExpenseRecorded",
            aggregateType = "Expense",
            aggregateId = savedExpense.id,
            tenantId = savedExpense.organizationId,
            routingKey = "expense.expense.recorded",
            payload = savedExpense
        )
        
        return savedExpense
    }
}
data class RecordExpenseCommand(val expense: Expense)

@Service
class VoidExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
    private val outboxRepository: ExpenseOutboxRepository
) {
    @Transactional
    fun execute(id: UUID, organizationId: UUID) {
        val expense = expenseRepository.findById(id, organizationId)
            ?: throw RuntimeException("Expense not found")
            
        expenseRepository.updateStatus(id, organizationId, ExpenseStatus.VOID)
        
        outboxRepository.save(
            eventId = UUID.randomUUID(),
            eventType = "ExpenseVoided",
            aggregateType = "Expense",
            aggregateId = id,
            tenantId = organizationId,
            routingKey = "expense.expense.voided",
            payload = mapOf("id" to id, "status" to "VOID")
        )
    }
}

@Service
class GetExpenseUseCase(private val expenseRepository: ExpenseRepository) {
    fun execute(id: UUID, organizationId: UUID): Expense? {
        return expenseRepository.findById(id, organizationId)
    }
}

@Service
class ListExpensesUseCase(private val expenseRepository: ExpenseRepository) {
    fun execute(organizationId: UUID, category: ExpenseCategory?, page: Int, size: Int): List<Expense> {
        return expenseRepository.findAll(organizationId, category, page, size)
    }
}
