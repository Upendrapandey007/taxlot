package com.taxlot.tax.service

import com.taxlot.platform.error.NotFoundException
import com.taxlot.tax.domain.TaxPeriod
import com.taxlot.tax.domain.TaxReturnStatus
import com.taxlot.tax.domain.TaxReturnSummary
import com.taxlot.tax.domain.TaxRule
import com.taxlot.tax.domain.TaxRuleRevision
import com.taxlot.tax.engine.TaxCalculationEngine
import com.taxlot.tax.engine.TaxCalculationRequest
import com.taxlot.tax.engine.TaxCalculationResult
import com.taxlot.tax.repository.RegulatoryScraperRepository
import com.taxlot.tax.repository.TaxPeriodRepository
import com.taxlot.tax.repository.TaxRuleRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.UUID

@Service
class TaxService(
    private val taxRuleRepository: TaxRuleRepository,
    private val scraperRepository: RegulatoryScraperRepository,
    private val periodRepository: TaxPeriodRepository
) {
    private val calculationEngine = TaxCalculationEngine()

    fun calculate(request: TaxCalculationRequest): TaxCalculationResult {
        val activeRules = taxRuleRepository.findActiveRules(request.countryCode, request.transactionDate)
        return calculationEngine.calculateTax(request, activeRules)
    }

    fun getActiveRules(countryCode: String, date: LocalDate?): List<TaxRule> {
        val effectiveDate = date ?: LocalDate.now()
        return taxRuleRepository.findActiveRules(countryCode, effectiveDate)
    }

    fun getTdsRates(countryCode: String): List<TaxRule> {
        val allRules = taxRuleRepository.findActiveRules(countryCode, LocalDate.now())
        return allRules.filter { it.categoryCode.startsWith("TDS_") }
    }

    fun getRecentRevisions(): List<TaxRuleRevision> {
        return scraperRepository.findRecentRevisions(50)
    }

    fun createPeriod(period: TaxPeriod): TaxPeriod {
        return periodRepository.createPeriod(period)
    }

    fun generateReturnSummary(
        organizationId: UUID,
        periodName: String,
        taxableSales: BigDecimal,
        exemptSales: BigDecimal,
        taxablePurchases: BigDecimal,
        exemptPurchases: BigDecimal
    ): TaxReturnSummary {
        val period = periodRepository.findByOrgAndPeriod(organizationId, periodName)
            ?: throw NotFoundException("TAX_PERIOD_NOT_FOUND", "Tax period $periodName not found for organization")

        val standardVatRate = BigDecimal("0.1300")
        val outputVat = taxableSales.multiply(standardVatRate).setScale(4, RoundingMode.HALF_UP)
        val inputVat = taxablePurchases.multiply(standardVatRate).setScale(4, RoundingMode.HALF_UP)
        val netVatPayable = outputVat.subtract(inputVat).setScale(4, RoundingMode.HALF_UP)

        val summary = TaxReturnSummary(
            organizationId = organizationId,
            periodId = period.id,
            totalTaxableSales = taxableSales.setScale(4),
            totalExemptSales = exemptSales.setScale(4),
            outputVatCollected = outputVat,
            totalTaxablePurchases = taxablePurchases.setScale(4),
            totalExemptPurchases = exemptPurchases.setScale(4),
            inputVatPaid = inputVat,
            netVatPayable = netVatPayable,
            totalTdsWithheld = BigDecimal.ZERO.setScale(4),
            status = TaxReturnStatus.DRAFT,
            notes = "Auto-generated return summary for $periodName"
        )
        return periodRepository.saveSummary(summary)
    }
}
