package com.taxlot.tax.domain

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

enum class TaxType {
    VAT,
    WITHHOLDING_TDS,
    INCOME_TAX,
    EXCISE
}

enum class ScraperType {
    IRD_PORTAL,
    GAZETTE,
    CIRCULAR_FEED,
    GENERIC_HTML
}

enum class ScrapeStatus {
    SUCCESS,
    FAILED,
    UNCHANGED
}

enum class RevisionChangeType {
    RATE_AMENDMENT,
    THRESHOLD_UPDATE,
    NEW_EXEMPTION,
    STATUTORY_UPDATE
}

enum class TaxPeriodStatus {
    OPEN,
    FILED,
    LOCKED
}

enum class TaxReturnStatus {
    DRAFT,
    FINALIZED,
    SUBMITTED
}

data class TaxJurisdiction(
    val id: UUID = UUID.randomUUID(),
    val countryCode: String,
    val name: String,
    val currencyCode: String,
    val isActive: Boolean = true,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

data class TaxCategory(
    val id: UUID = UUID.randomUUID(),
    val jurisdictionId: UUID,
    val code: String,
    val name: String,
    val description: String?,
    val taxType: TaxType,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

data class TaxRule(
    val id: UUID = UUID.randomUUID(),
    val jurisdictionId: UUID,
    val categoryCode: String,
    val name: String,
    val rate: BigDecimal,
    val thresholdAmount: BigDecimal? = null,
    val isReverseCharge: Boolean = false,
    val isExempt: Boolean = false,
    val legalReference: String,
    val description: String?,
    val effectiveFrom: LocalDate,
    val effectiveTo: LocalDate? = null,
    val isActive: Boolean = true,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
)

data class RegulatorySource(
    val id: UUID = UUID.randomUUID(),
    val jurisdictionId: UUID,
    val name: String,
    val sourceUrl: String,
    val scraperType: ScraperType,
    val lastScrapedAt: OffsetDateTime? = null,
    val lastContentHash: String? = null,
    val isActive: Boolean = true,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

data class RegulatoryScrape(
    val id: UUID = UUID.randomUUID(),
    val sourceId: UUID,
    val status: ScrapeStatus,
    val contentHash: String?,
    val scrapedContentSummary: String?,
    val changesDetected: Boolean = false,
    val errorMessage: String? = null,
    val scrapedAt: OffsetDateTime = OffsetDateTime.now()
)

data class TaxRuleRevision(
    val id: UUID = UUID.randomUUID(),
    val ruleId: UUID,
    val scrapeId: UUID? = null,
    val changeType: RevisionChangeType,
    val oldRate: BigDecimal?,
    val newRate: BigDecimal?,
    val oldThreshold: BigDecimal?,
    val newThreshold: BigDecimal?,
    val rationale: String,
    val effectiveFrom: LocalDate,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

data class TaxPeriod(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    val periodName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: TaxPeriodStatus = TaxPeriodStatus.OPEN,
    val filingDeadline: LocalDate,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

data class TaxReturnSummary(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    val periodId: UUID,
    val totalTaxableSales: BigDecimal,
    val totalExemptSales: BigDecimal,
    val outputVatCollected: BigDecimal,
    val totalTaxablePurchases: BigDecimal,
    val totalExemptPurchases: BigDecimal,
    val inputVatPaid: BigDecimal,
    val netVatPayable: BigDecimal,
    val totalTdsWithheld: BigDecimal,
    val status: TaxReturnStatus = TaxReturnStatus.DRAFT,
    val notes: String? = null,
    val generatedAt: OffsetDateTime = OffsetDateTime.now()
)
