package com.taxlot.tax.api

import com.taxlot.tax.domain.RegulatoryScrape
import com.taxlot.tax.domain.TaxPeriod
import com.taxlot.tax.domain.TaxReturnSummary
import com.taxlot.tax.domain.TaxRule
import com.taxlot.tax.domain.TaxRuleRevision
import com.taxlot.tax.engine.TaxCalculationRequest
import com.taxlot.tax.engine.TaxCalculationResult
import com.taxlot.tax.scraper.RegulatoryScraperService
import com.taxlot.tax.service.TaxService
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class CreatePeriodRequest(
    val organizationId: UUID,
    val periodName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val filingDeadline: LocalDate
)

data class GenerateSummaryRequest(
    val organizationId: UUID,
    val periodName: String,
    val taxableSales: BigDecimal,
    val exemptSales: BigDecimal = BigDecimal.ZERO,
    val taxablePurchases: BigDecimal,
    val exemptPurchases: BigDecimal = BigDecimal.ZERO
)

@RestController
@RequestMapping("/api/v1/tax")
class TaxController(
    private val taxService: TaxService,
    private val scraperService: RegulatoryScraperService
) {

    @PostMapping("/calculate")
    fun calculateTax(@Valid @RequestBody request: TaxCalculationRequest): TaxCalculationResult {
        return taxService.calculate(request)
    }

    @GetMapping("/rules")
    fun getRules(
        @RequestParam(defaultValue = "NP") countryCode: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): List<TaxRule> {
        return taxService.getActiveRules(countryCode, date)
    }

    @GetMapping("/tds-rates")
    fun getTdsRates(@RequestParam(defaultValue = "NP") countryCode: String): List<TaxRule> {
        return taxService.getTdsRates(countryCode)
    }

    @PostMapping("/scraper/trigger")
    fun triggerScraper(): List<RegulatoryScrape> {
        return scraperService.scrapeAllSources()
    }

    @GetMapping("/scraper/revisions")
    fun getRevisions(): List<TaxRuleRevision> {
        return taxService.getRecentRevisions()
    }

    @PostMapping("/periods")
    @ResponseStatus(HttpStatus.CREATED)
    fun createPeriod(@RequestBody request: CreatePeriodRequest): TaxPeriod {
        val period = TaxPeriod(
            organizationId = request.organizationId,
            periodName = request.periodName,
            startDate = request.startDate,
            endDate = request.endDate,
            filingDeadline = request.filingDeadline
        )
        return taxService.createPeriod(period)
    }

    @PostMapping("/returns/summary")
    fun generateReturnSummary(@RequestBody request: GenerateSummaryRequest): TaxReturnSummary {
        return taxService.generateReturnSummary(
            organizationId = request.organizationId,
            periodName = request.periodName,
            taxableSales = request.taxableSales,
            exemptSales = request.exemptSales,
            taxablePurchases = request.taxablePurchases,
            exemptPurchases = request.exemptPurchases
        )
    }
}
