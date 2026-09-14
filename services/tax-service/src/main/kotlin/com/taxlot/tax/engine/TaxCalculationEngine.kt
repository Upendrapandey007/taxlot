package com.taxlot.tax.engine

import com.taxlot.tax.domain.TaxRule
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

data class TaxLineItemRequest(
    val lineNumber: Int,
    val description: String,
    val quantity: BigDecimal = BigDecimal.ONE,
    val unitPrice: BigDecimal,
    val discount: BigDecimal = BigDecimal.ZERO,
    val categoryCode: String = "VAT_STANDARD",
    val isExport: Boolean = false,
    val isExempt: Boolean = false
)

data class TaxCalculationRequest(
    val countryCode: String = "NP",
    val transactionDate: LocalDate = LocalDate.now(),
    val isCustomerVatRegistered: Boolean = true,
    val isReverseChargeApplicable: Boolean = false,
    val items: List<TaxLineItemRequest>
)

data class TaxLineItemResult(
    val lineNumber: Int,
    val description: String,
    val grossAmount: BigDecimal,
    val taxableAmount: BigDecimal,
    val vatRate: BigDecimal,
    val vatAmount: BigDecimal,
    val tdsRate: BigDecimal,
    val tdsAmount: BigDecimal,
    val totalLineAmount: BigDecimal,
    val isExempt: Boolean,
    val isZeroRated: Boolean,
    val legalReference: String
)

data class TaxCalculationResult(
    val countryCode: String,
    val transactionDate: LocalDate,
    val subtotal: BigDecimal,
    val totalTaxableAmount: BigDecimal,
    val totalExemptAmount: BigDecimal,
    val totalVatAmount: BigDecimal,
    val totalTdsWithheld: BigDecimal,
    val grandTotal: BigDecimal,
    val netReceivableOrPayable: BigDecimal,
    val lines: List<TaxLineItemResult>
)

class TaxCalculationEngine {

    fun calculateTax(
        request: TaxCalculationRequest,
        activeRules: List<TaxRule>
    ): TaxCalculationResult {
        var subtotalAccumulator = BigDecimal.ZERO
        var taxableAccumulator = BigDecimal.ZERO
        var exemptAccumulator = BigDecimal.ZERO
        var vatAccumulator = BigDecimal.ZERO
        var tdsAccumulator = BigDecimal.ZERO
        var grandTotalAccumulator = BigDecimal.ZERO

        val ruleMap = activeRules.associateBy { it.categoryCode }

        val lineResults = request.items.map { item ->
            val gross = (item.quantity.multiply(item.unitPrice)).subtract(item.discount)
                .setScale(4, RoundingMode.HALF_UP)
            subtotalAccumulator = subtotalAccumulator.add(gross)

            // Resolve rule
            val category = when {
                item.isExport -> "VAT_ZERO"
                item.isExempt -> "VAT_EXEMPT"
                else -> item.categoryCode
            }

            val matchedRule = ruleMap[category] 
                ?: ruleMap["VAT_STANDARD"]
                ?: fallbackStandardRule()

            val isExempt = matchedRule.isExempt || item.isExempt
            val isZeroRated = matchedRule.categoryCode == "VAT_ZERO" || item.isExport

            val taxableAmount = if (isExempt) BigDecimal.ZERO.setScale(4) else gross
            if (isExempt) {
                exemptAccumulator = exemptAccumulator.add(gross)
            } else {
                taxableAccumulator = taxableAccumulator.add(taxableAmount)
            }

            // VAT calculation
            val vatRate = if (isExempt) BigDecimal.ZERO.setScale(4) else matchedRule.rate
            val vatAmount = taxableAmount.multiply(vatRate).setScale(4, RoundingMode.HALF_UP)
            vatAccumulator = vatAccumulator.add(vatAmount)

            // TDS Withholding check
            var tdsRate = BigDecimal.ZERO.setScale(4)
            var tdsAmount = BigDecimal.ZERO.setScale(4)

            // If item category is TDS specific
            if (matchedRule.categoryCode.startsWith("TDS_")) {
                val threshold = matchedRule.thresholdAmount
                val applies = threshold == null || gross >= threshold
                if (applies) {
                    tdsRate = matchedRule.rate
                    tdsAmount = gross.multiply(tdsRate).setScale(4, RoundingMode.HALF_UP)
                    tdsAccumulator = tdsAccumulator.add(tdsAmount)
                }
            }

            val lineTotal = gross.add(vatAmount)
            grandTotalAccumulator = grandTotalAccumulator.add(lineTotal)

            TaxLineItemResult(
                lineNumber = item.lineNumber,
                description = item.description,
                grossAmount = gross,
                taxableAmount = taxableAmount,
                vatRate = vatRate,
                vatAmount = vatAmount,
                tdsRate = tdsRate,
                tdsAmount = tdsAmount,
                totalLineAmount = lineTotal,
                isExempt = isExempt,
                isZeroRated = isZeroRated,
                legalReference = matchedRule.legalReference
            )
        }

        val netPayableOrReceivable = grandTotalAccumulator.subtract(tdsAccumulator)
            .setScale(4, RoundingMode.HALF_UP)

        return TaxCalculationResult(
            countryCode = request.countryCode,
            transactionDate = request.transactionDate,
            subtotal = subtotalAccumulator.setScale(4, RoundingMode.HALF_UP),
            totalTaxableAmount = taxableAccumulator.setScale(4, RoundingMode.HALF_UP),
            totalExemptAmount = exemptAccumulator.setScale(4, RoundingMode.HALF_UP),
            totalVatAmount = vatAccumulator.setScale(4, RoundingMode.HALF_UP),
            totalTdsWithheld = tdsAccumulator.setScale(4, RoundingMode.HALF_UP),
            grandTotal = grandTotalAccumulator.setScale(4, RoundingMode.HALF_UP),
            netReceivableOrPayable = netPayableOrReceivable,
            lines = lineResults
        )
    }

    private fun fallbackStandardRule(): TaxRule {
        return TaxRule(
            jurisdictionId = java.util.UUID.randomUUID(),
            categoryCode = "VAT_STANDARD",
            name = "Default 13% Standard VAT",
            rate = BigDecimal("0.1300"),
            legalReference = "Value Added Tax Act 2052, Section 7",
            description = "Standard rate fallback",
            effectiveFrom = LocalDate.of(2020, 1, 1)
        )
    }
}
