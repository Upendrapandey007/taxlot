package com.taxlot.reporting.engine

import com.taxlot.reporting.domain.AccountBalanceItem
import com.taxlot.reporting.domain.AccountCategory
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class FinancialReportEnginesTest : DescribeSpec({

    describe("ProfitAndLossEngine") {
        val engine = ProfitAndLossEngine()
        val orgId = UUID.randomUUID()
        val fromDate = LocalDate.of(2026, 1, 1)
        val toDate = LocalDate.of(2026, 1, 31)

        it("calculates revenue, COGS, gross margin and net income accurately") {
            val accounts = listOf(
                AccountBalanceItem(UUID.randomUUID(), "4000", "Sales Revenue", AccountCategory.REVENUE, BigDecimal("10000.00")),
                AccountBalanceItem(UUID.randomUUID(), "4100", "Service Revenue", AccountCategory.REVENUE, BigDecimal("5000.00")),
                AccountBalanceItem(UUID.randomUUID(), "5000", "Cost of Goods Sold", AccountCategory.EXPENSE, BigDecimal("4500.00")),
                AccountBalanceItem(UUID.randomUUID(), "5100", "Office Rent", AccountCategory.EXPENSE, BigDecimal("2000.00")),
                AccountBalanceItem(UUID.randomUUID(), "5200", "Salaries & Wages", AccountCategory.EXPENSE, BigDecimal("3000.00")),
                AccountBalanceItem(UUID.randomUUID(), "5900", "Income Tax Expense", AccountCategory.EXPENSE, BigDecimal("1100.00"))
            )

            val report = engine.generate(orgId, fromDate, toDate, accounts)

            report.operatingRevenue.subtotal.compareTo(BigDecimal("15000.0000")) shouldBe 0
            report.costOfGoodsSold.subtotal.compareTo(BigDecimal("4500.0000")) shouldBe 0
            report.grossProfit.compareTo(BigDecimal("10500.0000")) shouldBe 0
            report.grossMarginPercentage.compareTo(BigDecimal("70.0000")) shouldBe 0

            report.operatingExpenses.subtotal.compareTo(BigDecimal("5000.0000")) shouldBe 0
            report.operatingIncome.compareTo(BigDecimal("5500.0000")) shouldBe 0
            report.taxExpense.compareTo(BigDecimal("1100.0000")) shouldBe 0
            report.netIncome.compareTo(BigDecimal("4400.0000")) shouldBe 0
            report.netMarginPercentage.compareTo(BigDecimal("29.3333")) shouldBe 0
        }
    }

    describe("BalanceSheetEngine") {
        val engine = BalanceSheetEngine()
        val orgId = UUID.randomUUID()
        val asOfDate = LocalDate.of(2026, 1, 31)

        it("balances assets against liabilities plus equity with net income") {
            val accounts = listOf(
                AccountBalanceItem(UUID.randomUUID(), "1010", "Main Checking Account", AccountCategory.ASSET, BigDecimal("25000.00")),
                AccountBalanceItem(UUID.randomUUID(), "1200", "Accounts Receivable", AccountCategory.ASSET, BigDecimal("15000.00")),
                AccountBalanceItem(UUID.randomUUID(), "1500", "Equipment", AccountCategory.ASSET, BigDecimal("10000.00")),
                AccountBalanceItem(UUID.randomUUID(), "2000", "Accounts Payable", AccountCategory.LIABILITY, BigDecimal("8000.00")),
                AccountBalanceItem(UUID.randomUUID(), "2500", "Bank Loan", AccountCategory.LIABILITY, BigDecimal("12000.00")),
                AccountBalanceItem(UUID.randomUUID(), "3000", "Common Stock", AccountCategory.EQUITY, BigDecimal("20000.00")),
                AccountBalanceItem(UUID.randomUUID(), "3100", "Retained Earnings", AccountCategory.EQUITY, BigDecimal("5600.00"))
            )
            val currentNetIncome = BigDecimal("4400.00")

            val report = engine.generate(orgId, asOfDate, accounts, currentNetIncome)

            report.totalAssets.compareTo(BigDecimal("50000.0000")) shouldBe 0
            report.totalLiabilities.compareTo(BigDecimal("20000.0000")) shouldBe 0
            report.totalEquity.compareTo(BigDecimal("30000.0000")) shouldBe 0
            report.totalLiabilitiesAndEquity.compareTo(BigDecimal("50000.0000")) shouldBe 0
            report.isBalanced.shouldBeTrue()
        }
    }

    describe("AgingAnalysisEngine") {
        val engine = AgingAnalysisEngine()
        val orgId = UUID.randomUUID()
        val asOfDate = LocalDate.of(2026, 3, 1)

        it("categorizes invoices into current, 1-30, 31-60, 61-90, 90+ buckets") {
            val custId = UUID.randomUUID()
            val invoices = listOf(
                AgingAnalysisEngine.RawInvoiceForAging(UUID.randomUUID(), custId, "Acme Corp", "INV-101", LocalDate.of(2026, 3, 15), BigDecimal("1000.00")),
                AgingAnalysisEngine.RawInvoiceForAging(UUID.randomUUID(), custId, "Acme Corp", "INV-102", LocalDate.of(2026, 2, 20), BigDecimal("500.00")),
                AgingAnalysisEngine.RawInvoiceForAging(UUID.randomUUID(), custId, "Acme Corp", "INV-103", LocalDate.of(2026, 1, 20), BigDecimal("300.00")),
                AgingAnalysisEngine.RawInvoiceForAging(UUID.randomUUID(), custId, "Acme Corp", "INV-104", LocalDate.of(2025, 12, 1), BigDecimal("250.00"))
            )

            val report = engine.calculateArAging(orgId, asOfDate, invoices)

            report.totalReceivable.compareTo(BigDecimal("2050.0000")) shouldBe 0
            report.buckets.size shouldBe 5
            report.customerSummaries.size shouldBe 1

            val summary = report.customerSummaries.first()
            summary.current.compareTo(BigDecimal("1000.0000")) shouldBe 0
            summary.days1to30.compareTo(BigDecimal("500.0000")) shouldBe 0
            summary.days31to60.compareTo(BigDecimal("300.0000")) shouldBe 0
            summary.daysOver90.compareTo(BigDecimal("250.0000")) shouldBe 0
        }
    }
})
