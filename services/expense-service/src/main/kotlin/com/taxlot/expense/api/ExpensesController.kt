package com.taxlot.expense.api

import com.taxlot.expense.domain.*
import com.taxlot.expense.usecase.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/expenses")
class ExpensesController(
    private val recordExpenseUseCase: RecordExpenseUseCase,
    private val voidExpenseUseCase: VoidExpenseUseCase,
    private val getExpenseUseCase: GetExpenseUseCase,
    private val listExpensesUseCase: ListExpensesUseCase
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun recordExpense(
        @RequestHeader("X-Tenant-Id") tenantId: UUID,
        @RequestBody request: Expense
    ): Expense {
        val expense = request.copy(organizationId = tenantId, id = UUID.randomUUID())
        return recordExpenseUseCase.execute(RecordExpenseCommand(expense))
    }

    @GetMapping("/{id}")
    fun getExpense(
        @RequestHeader("X-Tenant-Id") tenantId: UUID,
        @PathVariable id: UUID
    ): Expense {
        return getExpenseUseCase.execute(id, tenantId) ?: throw RuntimeException("Not found")
    }

    @GetMapping
    fun listExpenses(
        @RequestHeader("X-Tenant-Id") tenantId: UUID,
        @RequestParam(required = false) category: ExpenseCategory?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): List<Expense> {
        return listExpensesUseCase.execute(tenantId, category, page, size)
    }

    @PostMapping("/{id}/void")
    fun voidExpense(
        @RequestHeader("X-Tenant-Id") tenantId: UUID,
        @PathVariable id: UUID
    ) {
        voidExpenseUseCase.execute(id, tenantId)
    }
}
