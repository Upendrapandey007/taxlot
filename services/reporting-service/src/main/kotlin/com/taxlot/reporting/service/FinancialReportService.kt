package com.taxlot.reporting.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.reporting.client.AccountingDataClient
import com.taxlot.reporting.client.InvoiceDataClient
import com.taxlot.reporting.domain.*
import com.taxlot.reporting.engine.AgingAnalysisEngine
import com.taxlot.reporting.engine.BalanceSheetEngine
import com.taxlot.reporting.engine.ProfitAndLossEngine
import com.taxlot.reporting.repository.ReportSnapshotRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.UUID

@Service
class FinancialReportService(
    private val accountingClient: AccountingDataClient,
    private val invoiceClient: InvoiceDataClient,
    private val snapshotRepository: ReportSnapshotRepository,
    private val objectMapper: ObjectMapper
) {
    private val pnlEngine = ProfitAndLossEngine()
    private val balanceSheetEngine = BalanceSheetEngine()
    private val agingEngine = AgingAnalysisEngine()

    fun getProfitAndLoss(
        organizationId: UUID,
        fromDate: LocalDate,
        toDate: LocalDate,
        periodId: UUID? = null,
        saveSnapshot: Boolean = false
    ): ProfitAndLossReport {
        val accounts = accountingClient.getTrialBalanceAccounts(organizationId, periodId)
        val report = pnlEngine.generate(organizationId, fromDate, toDate, accounts)

        if (saveSnapshot) {
            val json = objectMapper.writeValueAsString(report)
            snapshotRepository.saveSnapshot(
                FinancialReportSnapshot(
                    organizationId = organizationId,
                    reportType = ReportType.PROFIT_AND_LOSS,
                    periodId = periodId,
                    fromDate = fromDate,
                    toDate = toDate,
                    currencyCode = report.currencyCode,
                    reportData = json,
                    generatedBy = null
                )
            )
        }
        return report
    }

    fun getBalanceSheet(
        organizationId: UUID,
        asOfDate: LocalDate,
        periodId: UUID? = null,
        saveSnapshot: Boolean = false
    ): BalanceSheetReport {
        val accounts = accountingClient.getTrialBalanceAccounts(organizationId, periodId)
        // Compute net income for current period to integrate with equity if applicable
        val pnl = pnlEngine.generate(organizationId, asOfDate.withDayOfMonth(1), asOfDate, accounts)
        val report = balanceSheetEngine.generate(organizationId, asOfDate, accounts, pnl.netIncome)

        if (saveSnapshot) {
            val json = objectMapper.writeValueAsString(report)
            snapshotRepository.saveSnapshot(
                FinancialReportSnapshot(
                    organizationId = organizationId,
                    reportType = ReportType.BALANCE_SHEET,
                    periodId = periodId,
                    fromDate = asOfDate.withDayOfMonth(1),
                    toDate = asOfDate,
                    currencyCode = report.currencyCode,
                    reportData = json,
                    generatedBy = null
                )
            )
        }
        return report
    }

    fun getCashFlow(
        organizationId: UUID,
        fromDate: LocalDate,
        toDate: LocalDate,
        periodId: UUID? = null
    ): CashFlowReport {
        val accounts = accountingClient.getTrialBalanceAccounts(organizationId, periodId)
        val pnl = pnlEngine.generate(organizationId, fromDate, toDate, accounts)

        val cashAccounts = accounts.filter { it.category == AccountCategory.ASSET && it.code.startsWith("10") }
        val endingCash = cashAccounts.fold(BigDecimal.ZERO) { acc, a -> acc.add(a.balance) }

        val operatingLines = listOf(
            ReportLineItem("NET_INCOME", "Net Income", pnl.netIncome),
            ReportLineItem("DEPRECIATION", "Depreciation & Amortization (Non-Cash)", BigDecimal.ZERO.setScale(4))
        )
        val netCashOperations = pnl.netIncome

        return CashFlowReport(
            organizationId = organizationId,
            fromDate = fromDate,
            toDate = toDate,
            currencyCode = "USD",
            operatingActivities = ReportSection("Operating Activities", operatingLines, netCashOperations),
            netCashFromOperations = netCashOperations,
            investingActivities = ReportSection("Investing Activities", emptyList(), BigDecimal.ZERO.setScale(4)),
            netCashFromInvesting = BigDecimal.ZERO.setScale(4),
            financingActivities = ReportSection("Financing Activities", emptyList(), BigDecimal.ZERO.setScale(4)),
            netCashFromFinancing = BigDecimal.ZERO.setScale(4),
            netChangeInCash = netCashOperations,
            beginningCashBalance = endingCash.subtract(netCashOperations).setScale(4, RoundingMode.HALF_UP),
            endingCashBalance = endingCash.setScale(4, RoundingMode.HALF_UP)
        )
    }

    fun getArAging(organizationId: UUID, asOfDate: LocalDate): ArAgingReport {
        val invoices = invoiceClient.getOutstandingInvoices(organizationId)
        return agingEngine.calculateArAging(organizationId, asOfDate, invoices)
    }

    fun getKpis(organizationId: UUID, asOfDate: LocalDate = LocalDate.now()): FinancialKpiSummary {
        val accounts = accountingClient.getTrialBalanceAccounts(organizationId, null)
        val pnl = pnlEngine.generate(organizationId, asOfDate.withDayOfMonth(1), asOfDate, accounts)
        val balanceSheet = balanceSheetEngine.generate(organizationId, asOfDate, accounts, pnl.netIncome)
        val arAging = getArAging(organizationId, asOfDate)

        val cashAccounts = accounts.filter { it.category == AccountCategory.ASSET && it.code.startsWith("10") }
        val cashBalance = cashAccounts.fold(BigDecimal.ZERO) { acc, a -> acc.add(a.balance) }

        // Current Ratio = Current Assets / Current Liabilities
        val currentLiabilities = balanceSheet.currentLiabilities.subtotal
        val currentRatio = if (currentLiabilities.compareTo(BigDecimal.ZERO) > 0) {
            balanceSheet.currentAssets.subtotal.divide(currentLiabilities, 4, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO.setScale(4)
        }

        // Quick Ratio = (Cash + AR) / Current Liabilities
        val quickAssets = cashBalance.add(arAging.totalReceivable)
        val quickRatio = if (currentLiabilities.compareTo(BigDecimal.ZERO) > 0) {
            quickAssets.divide(currentLiabilities, 4, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO.setScale(4)
        }

        val apAccounts = accounts.filter { it.category == AccountCategory.LIABILITY && it.code.startsWith("20") }
        val apOutstanding = apAccounts.fold(BigDecimal.ZERO) { acc, a -> acc.add(a.balance.abs()) }

        val kpi = FinancialKpiSummary(
            organizationId = organizationId,
            asOfDate = asOfDate,
            totalRevenue = pnl.operatingRevenue.subtotal,
            totalExpenses = pnl.operatingExpenses.subtotal.add(pnl.costOfGoodsSold.subtotal),
            netIncome = pnl.netIncome,
            grossProfitMargin = pnl.grossMarginPercentage,
            netProfitMargin = pnl.netMarginPercentage,
            currentRatio = currentRatio,
            quickRatio = quickRatio,
            cashBalance = cashBalance,
            arOutstanding = arAging.totalReceivable,
            apOutstanding = apOutstanding
        )

        snapshotRepository.saveKpiSnapshot(kpi)
        return kpi
    }

    fun getSnapshots(
        organizationId: UUID,
        reportType: ReportType? = null,
        fromDate: LocalDate? = null,
        toDate: LocalDate? = null
    ): List<FinancialReportSnapshot> {
        return snapshotRepository.findSnapshots(organizationId, reportType, fromDate, toDate)
    }
}
