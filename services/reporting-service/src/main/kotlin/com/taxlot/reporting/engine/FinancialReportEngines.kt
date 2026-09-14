package com.taxlot.reporting.engine

import com.taxlot.reporting.domain.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

class ProfitAndLossEngine {

    fun generate(
        organizationId: UUID,
        fromDate: LocalDate,
        toDate: LocalDate,
        accounts: List<AccountBalanceItem>
    ): ProfitAndLossReport {
        // Group Revenue accounts (Code 4xxx)
        val revenueItems = accounts
            .filter { it.category == AccountCategory.REVENUE }
            .map { ReportLineItem(it.code, it.name, it.balance.abs()) }
        val totalRevenue = revenueItems.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.amount) }
            .setScale(4, RoundingMode.HALF_UP)

        // Group COGS accounts (Code 50xx)
        val cogsItems = accounts
            .filter { it.category == AccountCategory.EXPENSE && it.code.startsWith("50") }
            .map { ReportLineItem(it.code, it.name, it.balance.abs()) }
        val totalCogs = cogsItems.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.amount) }
            .setScale(4, RoundingMode.HALF_UP)

        val grossProfit = totalRevenue.subtract(totalCogs).setScale(4, RoundingMode.HALF_UP)
        val grossMarginPct = calculatePercentage(grossProfit, totalRevenue)

        // Operating Expenses (Code 5xxx, 6xxx not 50xx)
        val opexItems = accounts
            .filter { it.category == AccountCategory.EXPENSE && !it.code.startsWith("50") && !it.code.startsWith("59") }
            .map { 
                val pct = calculatePercentage(it.balance.abs(), totalRevenue)
                ReportLineItem(it.code, it.name, it.balance.abs(), pct) 
            }
        val totalOpex = opexItems.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.amount) }
            .setScale(4, RoundingMode.HALF_UP)

        val operatingIncome = grossProfit.subtract(totalOpex).setScale(4, RoundingMode.HALF_UP)
        val operatingMarginPct = calculatePercentage(operatingIncome, totalRevenue)

        // Tax Expense (Code 59xx)
        val taxAccounts = accounts.filter { it.category == AccountCategory.EXPENSE && it.code.startsWith("59") }
        val taxExpense = taxAccounts.fold(BigDecimal.ZERO) { acc, it -> acc.add(it.balance.abs()) }
            .setScale(4, RoundingMode.HALF_UP)

        val netIncome = operatingIncome.subtract(taxExpense).setScale(4, RoundingMode.HALF_UP)
        val netMarginPct = calculatePercentage(netIncome, totalRevenue)

        return ProfitAndLossReport(
            organizationId = organizationId,
            fromDate = fromDate,
            toDate = toDate,
            operatingRevenue = ReportSection("Operating Revenue", revenueItems, totalRevenue),
            costOfGoodsSold = ReportSection("Cost of Goods Sold", cogsItems, totalCogs),
            grossProfit = grossProfit,
            grossMarginPercentage = grossMarginPct,
            operatingExpenses = ReportSection("Operating Expenses", opexItems, totalOpex),
            operatingIncome = operatingIncome,
            operatingMarginPercentage = operatingMarginPct,
            taxExpense = taxExpense,
            netIncome = netIncome,
            netMarginPercentage = netMarginPct
        )
    }

    private fun calculatePercentage(part: BigDecimal, whole: BigDecimal): BigDecimal {
        if (whole.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO.setScale(4)
        return part.divide(whole, 6, RoundingMode.HALF_UP)
            .multiply(BigDecimal("100"))
            .setScale(4, RoundingMode.HALF_UP)
    }
}

class BalanceSheetEngine {

    fun generate(
        organizationId: UUID,
        asOfDate: LocalDate,
        accounts: List<AccountBalanceItem>,
        currentPeriodNetIncome: BigDecimal = BigDecimal.ZERO
    ): BalanceSheetReport {
        // Current Assets: Cash (10xx), AR (12xx), Inventory/Prepaids (13xx-14xx)
        val currentAssetItems = accounts
            .filter { it.category == AccountCategory.ASSET && !it.code.startsWith("15") && !it.code.startsWith("16") }
            .map { ReportLineItem(it.code, it.name, it.balance) }
        val currentAssetsSubtotal = currentAssetItems.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.amount) }

        // Non-Current Assets: Equipment, Property (15xx, 16xx)
        val nonCurrentAssetItems = accounts
            .filter { it.category == AccountCategory.ASSET && (it.code.startsWith("15") || it.code.startsWith("16")) }
            .map { ReportLineItem(it.code, it.name, it.balance) }
        val nonCurrentAssetsSubtotal = nonCurrentAssetItems.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.amount) }

        val totalAssets = currentAssetsSubtotal.add(nonCurrentAssetsSubtotal).setScale(4, RoundingMode.HALF_UP)

        // Current Liabilities: AP (20xx), Tax Payable (21xx-22xx)
        val currentLiabilityItems = accounts
            .filter { it.category == AccountCategory.LIABILITY && !it.code.startsWith("25") }
            .map { ReportLineItem(it.code, it.name, it.balance.abs()) }
        val currentLiabilitiesSubtotal = currentLiabilityItems.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.amount) }

        // Long Term Liabilities (25xx)
        val longTermLiabilityItems = accounts
            .filter { it.category == AccountCategory.LIABILITY && it.code.startsWith("25") }
            .map { ReportLineItem(it.code, it.name, it.balance.abs()) }
        val longTermLiabilitiesSubtotal = longTermLiabilityItems.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.amount) }

        val totalLiabilities = currentLiabilitiesSubtotal.add(longTermLiabilitiesSubtotal).setScale(4, RoundingMode.HALF_UP)

        // Equity: Share Capital (30xx), Retained Earnings (31xx) + Current Period Net Income
        val equityItems = accounts
            .filter { it.category == AccountCategory.EQUITY }
            .map { ReportLineItem(it.code, it.name, it.balance.abs()) }
            .toMutableList()

        if (currentPeriodNetIncome.compareTo(BigDecimal.ZERO) != 0) {
            equityItems.add(ReportLineItem("3999", "Current Period Net Income", currentPeriodNetIncome))
        }

        val totalEquity = equityItems.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.amount) }
            .setScale(4, RoundingMode.HALF_UP)

        val totalLiabilitiesAndEquity = totalLiabilities.add(totalEquity).setScale(4, RoundingMode.HALF_UP)
        val isBalanced = totalAssets.compareTo(totalLiabilitiesAndEquity) == 0

        return BalanceSheetReport(
            organizationId = organizationId,
            asOfDate = asOfDate,
            currentAssets = ReportSection("Current Assets", currentAssetItems, currentAssetsSubtotal.setScale(4)),
            nonCurrentAssets = ReportSection("Non-Current Assets", nonCurrentAssetItems, nonCurrentAssetsSubtotal.setScale(4)),
            totalAssets = totalAssets,
            currentLiabilities = ReportSection("Current Liabilities", currentLiabilityItems, currentLiabilitiesSubtotal.setScale(4)),
            longTermLiabilities = ReportSection("Long-Term Liabilities", longTermLiabilityItems, longTermLiabilitiesSubtotal.setScale(4)),
            totalLiabilities = totalLiabilities,
            equity = ReportSection("Owner's Equity", equityItems, totalEquity),
            totalEquity = totalEquity,
            totalLiabilitiesAndEquity = totalLiabilitiesAndEquity,
            isBalanced = isBalanced
        )
    }
}

class AgingAnalysisEngine {

    data class RawInvoiceForAging(
        val invoiceId: UUID,
        val customerId: UUID,
        val customerName: String,
        val invoiceNumber: String,
        val dueDate: LocalDate,
        val outstandingBalance: BigDecimal
    )

    fun calculateArAging(
        organizationId: UUID,
        asOfDate: LocalDate,
        invoices: List<RawInvoiceForAging>
    ): ArAgingReport {
        var currentBucketTotal = BigDecimal.ZERO
        var b1to30Total = BigDecimal.ZERO
        var b31to60Total = BigDecimal.ZERO
        var b61to90Total = BigDecimal.ZERO
        var b90PlusTotal = BigDecimal.ZERO

        var currentCount = 0
        var b1to30Count = 0
        var b31to60Count = 0
        var b61to90Count = 0
        var b90PlusCount = 0

        val customerGroup = invoices.groupBy { it.customerId }
        val customerSummaries = mutableListOf<CustomerAgingSummary>()

        for ((custId, customerInvoices) in customerGroup) {
            val custName = customerInvoices.first().customerName
            var cCurrent = BigDecimal.ZERO
            var c1to30 = BigDecimal.ZERO
            var c31to60 = BigDecimal.ZERO
            var c61to90 = BigDecimal.ZERO
            var c90Plus = BigDecimal.ZERO

            for (inv in customerInvoices) {
                val daysPastDue = ChronoUnit.DAYS.between(inv.dueDate, asOfDate)
                val bal = inv.outstandingBalance

                when {
                    daysPastDue <= 0 -> {
                        cCurrent = cCurrent.add(bal)
                        currentBucketTotal = currentBucketTotal.add(bal)
                        currentCount++
                    }
                    daysPastDue in 1..30 -> {
                        c1to30 = c1to30.add(bal)
                        b1to30Total = b1to30Total.add(bal)
                        b1to30Count++
                    }
                    daysPastDue in 31..60 -> {
                        c31to60 = c31to60.add(bal)
                        b31to60Total = b31to60Total.add(bal)
                        b31to60Count++
                    }
                    daysPastDue in 61..90 -> {
                        c61to90 = c61to90.add(bal)
                        b61to90Total = b61to90Total.add(bal)
                        b61to90Count++
                    }
                    else -> {
                        c90Plus = c90Plus.add(bal)
                        b90PlusTotal = b90PlusTotal.add(bal)
                        b90PlusCount++
                    }
                }
            }

            val custTotal = cCurrent.add(c1to30).add(c31to60).add(c61to90).add(c90Plus)
                .setScale(4, RoundingMode.HALF_UP)

            customerSummaries.add(
                CustomerAgingSummary(
                    customerId = custId,
                    customerName = custName,
                    current = cCurrent.setScale(4),
                    days1to30 = c1to30.setScale(4),
                    days31to60 = c31to60.setScale(4),
                    days61to90 = c61to90.setScale(4),
                    daysOver90 = c90Plus.setScale(4),
                    totalOutstanding = custTotal
                )
            )
        }

        val buckets = listOf(
            AgingBucket("Current", currentCount, currentBucketTotal.setScale(4, RoundingMode.HALF_UP)),
            AgingBucket("1-30 Days", b1to30Count, b1to30Total.setScale(4, RoundingMode.HALF_UP)),
            AgingBucket("31-60 Days", b31to60Count, b31to60Total.setScale(4, RoundingMode.HALF_UP)),
            AgingBucket("61-90 Days", b61to90Count, b61to90Total.setScale(4, RoundingMode.HALF_UP)),
            AgingBucket("90+ Days", b90PlusCount, b90PlusTotal.setScale(4, RoundingMode.HALF_UP))
        )

        val totalReceivable = buckets.fold(BigDecimal.ZERO) { acc, b -> acc.add(b.totalOutstanding) }
            .setScale(4, RoundingMode.HALF_UP)

        return ArAgingReport(
            organizationId = organizationId,
            asOfDate = asOfDate,
            totalReceivable = totalReceivable,
            buckets = buckets,
            customerSummaries = customerSummaries.sortedByDescending { it.totalOutstanding }
        )
    }
}
