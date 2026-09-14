package com.taxlot.reporting.domain

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

enum class ReportType {
    PROFIT_AND_LOSS,
    BALANCE_SHEET,
    CASH_FLOW,
    TRIAL_BALANCE,
    AR_AGING,
    AP_AGING
}

enum class AccountCategory {
    ASSET,
    LIABILITY,
    EQUITY,
    REVENUE,
    EXPENSE
}

data class AccountBalanceItem(
    val accountId: UUID,
    val code: String,
    val name: String,
    val category: AccountCategory,
    val balance: BigDecimal
)

data class ReportLineItem(
    val code: String,
    val name: String,
    val amount: BigDecimal,
    val percentageOfRevenue: BigDecimal? = null
)

data class ReportSection(
    val title: String,
    val lines: List<ReportLineItem>,
    val subtotal: BigDecimal
)

data class ProfitAndLossReport(
    val organizationId: UUID,
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val currencyCode: String = "USD",
    val operatingRevenue: ReportSection,
    val costOfGoodsSold: ReportSection,
    val grossProfit: BigDecimal,
    val grossMarginPercentage: BigDecimal,
    val operatingExpenses: ReportSection,
    val operatingIncome: BigDecimal,
    val operatingMarginPercentage: BigDecimal,
    val taxExpense: BigDecimal,
    val netIncome: BigDecimal,
    val netMarginPercentage: BigDecimal
)

data class BalanceSheetReport(
    val organizationId: UUID,
    val asOfDate: LocalDate,
    val currencyCode: String = "USD",
    val currentAssets: ReportSection,
    val nonCurrentAssets: ReportSection,
    val totalAssets: BigDecimal,
    val currentLiabilities: ReportSection,
    val longTermLiabilities: ReportSection,
    val totalLiabilities: BigDecimal,
    val equity: ReportSection,
    val totalEquity: BigDecimal,
    val totalLiabilitiesAndEquity: BigDecimal,
    val isBalanced: Boolean
)

data class CashFlowReport(
    val organizationId: UUID,
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val currencyCode: String = "USD",
    val operatingActivities: ReportSection,
    val netCashFromOperations: BigDecimal,
    val investingActivities: ReportSection,
    val netCashFromInvesting: BigDecimal,
    val financingActivities: ReportSection,
    val netCashFromFinancing: BigDecimal,
    val netChangeInCash: BigDecimal,
    val beginningCashBalance: BigDecimal,
    val endingCashBalance: BigDecimal
)

data class AgingBucket(
    val bucketName: String,
    val invoiceCount: Int,
    val totalOutstanding: BigDecimal
)

data class CustomerAgingSummary(
    val customerId: UUID,
    val customerName: String,
    val current: BigDecimal,
    val days1to30: BigDecimal,
    val days31to60: BigDecimal,
    val days61to90: BigDecimal,
    val daysOver90: BigDecimal,
    val totalOutstanding: BigDecimal
)

data class ArAgingReport(
    val organizationId: UUID,
    val asOfDate: LocalDate,
    val currencyCode: String = "USD",
    val totalReceivable: BigDecimal,
    val buckets: List<AgingBucket>,
    val customerSummaries: List<CustomerAgingSummary>
)

data class FinancialKpiSummary(
    val organizationId: UUID,
    val asOfDate: LocalDate,
    val totalRevenue: BigDecimal,
    val totalExpenses: BigDecimal,
    val netIncome: BigDecimal,
    val grossProfitMargin: BigDecimal,
    val netProfitMargin: BigDecimal,
    val currentRatio: BigDecimal,
    val quickRatio: BigDecimal,
    val cashBalance: BigDecimal,
    val arOutstanding: BigDecimal,
    val apOutstanding: BigDecimal
)

data class FinancialReportSnapshot(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    val reportType: ReportType,
    val periodId: UUID?,
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val currencyCode: String = "USD",
    val reportData: String,
    val generatedBy: UUID?,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
