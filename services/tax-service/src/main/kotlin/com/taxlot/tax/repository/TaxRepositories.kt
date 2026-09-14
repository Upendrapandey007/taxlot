package com.taxlot.tax.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.tax.domain.*
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class TaxJurisdictionRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {
    fun findByCountryCode(countryCode: String): TaxJurisdiction? {
        val sql = "SELECT * FROM tax_jurisdictions WHERE country_code = :code AND is_active = true"
        return jdbcTemplate.query(sql, MapSqlParameterSource("code", countryCode)) { rs, _ ->
            TaxJurisdiction(
                id = rs.getObject("id", UUID::class.java),
                countryCode = rs.getString("country_code"),
                name = rs.getString("name"),
                currencyCode = rs.getString("currency_code"),
                isActive = rs.getBoolean("is_active"),
                createdAt = rs.getObject("created_at", OffsetDateTime::class.java)
            )
        }.firstOrNull()
    }
}

@Repository
class TaxRuleRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun findActiveRules(countryCode: String, effectiveDate: LocalDate): List<TaxRule> {
        val sql = """
            SELECT r.* FROM tax_rules r
            JOIN tax_jurisdictions j ON r.jurisdiction_id = j.id
            WHERE j.country_code = :countryCode
              AND r.is_active = true
              AND r.effective_from <= :effectiveDate
              AND (r.effective_to IS NULL OR r.effective_to >= :effectiveDate)
            ORDER BY r.category_code ASC, r.effective_from DESC
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("countryCode", countryCode)
            .addValue("effectiveDate", effectiveDate)

        return jdbcTemplate.query(sql, params) { rs, _ -> mapRowToTaxRule(rs) }
    }

    fun findById(id: UUID): TaxRule? {
        val sql = "SELECT * FROM tax_rules WHERE id = :id"
        return jdbcTemplate.query(sql, MapSqlParameterSource("id", id)) { rs, _ ->
            mapRowToTaxRule(rs)
        }.firstOrNull()
    }

    fun findByCategory(jurisdictionId: UUID, categoryCode: String): TaxRule? {
        val sql = """
            SELECT * FROM tax_rules 
            WHERE jurisdiction_id = :jurisdictionId AND category_code = :categoryCode AND is_active = true
            ORDER BY effective_from DESC LIMIT 1
        """.trimIndent()
        return jdbcTemplate.query(
            sql, 
            MapSqlParameterSource("jurisdictionId", jurisdictionId).addValue("categoryCode", categoryCode)
        ) { rs, _ -> mapRowToTaxRule(rs) }.firstOrNull()
    }

    fun save(rule: TaxRule): TaxRule {
        val sql = """
            INSERT INTO tax_rules (
                id, jurisdiction_id, category_code, name, rate, threshold_amount,
                is_reverse_charge, is_exempt, legal_reference, description,
                effective_from, effective_to, is_active, created_at, updated_at
            ) VALUES (
                :id, :jurisdictionId, :categoryCode, :name, :rate, :thresholdAmount,
                :isReverseCharge, :isExempt, :legalReference, :description,
                :effectiveFrom, :effectiveTo, :isActive, :createdAt, :updatedAt
            )
            ON CONFLICT (id) DO UPDATE SET
                rate = EXCLUDED.rate,
                threshold_amount = EXCLUDED.threshold_amount,
                legal_reference = EXCLUDED.legal_reference,
                effective_to = EXCLUDED.effective_to,
                is_active = EXCLUDED.is_active,
                updated_at = NOW()
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("id", rule.id)
            .addValue("jurisdictionId", rule.jurisdictionId)
            .addValue("categoryCode", rule.categoryCode)
            .addValue("name", rule.name)
            .addValue("rate", rule.rate)
            .addValue("thresholdAmount", rule.thresholdAmount)
            .addValue("isReverseCharge", rule.isReverseCharge)
            .addValue("isExempt", rule.isExempt)
            .addValue("legalReference", rule.legalReference)
            .addValue("description", rule.description)
            .addValue("effectiveFrom", rule.effectiveFrom)
            .addValue("effectiveTo", rule.effectiveTo)
            .addValue("isActive", rule.isActive)
            .addValue("createdAt", rule.createdAt)
            .addValue("updatedAt", rule.updatedAt)

        jdbcTemplate.update(sql, params)
        return rule
    }

    private fun mapRowToTaxRule(rs: ResultSet): TaxRule {
        return TaxRule(
            id = rs.getObject("id", UUID::class.java),
            jurisdictionId = rs.getObject("jurisdiction_id", UUID::class.java),
            categoryCode = rs.getString("category_code"),
            name = rs.getString("name"),
            rate = rs.getBigDecimal("rate"),
            thresholdAmount = rs.getBigDecimal("threshold_amount"),
            isReverseCharge = rs.getBoolean("is_reverse_charge"),
            isExempt = rs.getBoolean("is_exempt"),
            legalReference = rs.getString("legal_reference"),
            description = rs.getString("description"),
            effectiveFrom = rs.getDate("effective_from").toLocalDate(),
            effectiveTo = rs.getDate("effective_to")?.toLocalDate(),
            isActive = rs.getBoolean("is_active"),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java)
        )
    }
}

@Repository
class RegulatoryScraperRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun findAllActiveSources(): List<RegulatorySource> {
        val sql = "SELECT * FROM regulatory_sources WHERE is_active = true"
        return jdbcTemplate.query(sql, MapSqlParameterSource()) { rs, _ ->
            RegulatorySource(
                id = rs.getObject("id", UUID::class.java),
                jurisdictionId = rs.getObject("jurisdiction_id", UUID::class.java),
                name = rs.getString("name"),
                sourceUrl = rs.getString("source_url"),
                scraperType = ScraperType.valueOf(rs.getString("scraper_type")),
                lastScrapedAt = rs.getObject("last_scraped_at", OffsetDateTime::class.java),
                lastContentHash = rs.getString("last_content_hash"),
                isActive = rs.getBoolean("is_active"),
                createdAt = rs.getObject("created_at", OffsetDateTime::class.java)
            )
        }
    }

    fun updateSourceHash(sourceId: UUID, hash: String, scrapedAt: OffsetDateTime) {
        val sql = """
            UPDATE regulatory_sources 
            SET last_content_hash = :hash, last_scraped_at = :scrapedAt 
            WHERE id = :id
        """.trimIndent()
        jdbcTemplate.update(
            sql,
            MapSqlParameterSource("id", sourceId)
                .addValue("hash", hash)
                .addValue("scrapedAt", scrapedAt)
        )
    }

    fun recordScrape(scrape: RegulatoryScrape): RegulatoryScrape {
        val sql = """
            INSERT INTO regulatory_scrapes (
                id, source_id, status, content_hash, scraped_content_summary,
                changes_detected, error_message, scraped_at
            ) VALUES (
                :id, :sourceId, :status, :contentHash, :summary,
                :changesDetected, :errorMessage, :scrapedAt
            )
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            MapSqlParameterSource()
                .addValue("id", scrape.id)
                .addValue("sourceId", scrape.sourceId)
                .addValue("status", scrape.status.name)
                .addValue("contentHash", scrape.contentHash)
                .addValue("summary", scrape.scrapedContentSummary)
                .addValue("changesDetected", scrape.changesDetected)
                .addValue("errorMessage", scrape.errorMessage)
                .addValue("scrapedAt", scrape.scrapedAt)
        )
        return scrape
    }

    fun recordRevision(revision: TaxRuleRevision): TaxRuleRevision {
        val sql = """
            INSERT INTO tax_rule_revisions (
                id, rule_id, scrape_id, change_type, old_rate, new_rate,
                old_threshold, new_threshold, rationale, effective_from, created_at
            ) VALUES (
                :id, :ruleId, :scrapeId, :changeType, :oldRate, :newRate,
                :oldThreshold, :newThreshold, :rationale, :effectiveFrom, :createdAt
            )
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            MapSqlParameterSource()
                .addValue("id", revision.id)
                .addValue("ruleId", revision.ruleId)
                .addValue("scrapeId", revision.scrapeId)
                .addValue("changeType", revision.changeType.name)
                .addValue("oldRate", revision.oldRate)
                .addValue("newRate", revision.newRate)
                .addValue("oldThreshold", revision.oldThreshold)
                .addValue("newThreshold", revision.newThreshold)
                .addValue("rationale", revision.rationale)
                .addValue("effectiveFrom", revision.effectiveFrom)
                .addValue("createdAt", revision.createdAt)
        )
        return revision
    }

    fun findRecentRevisions(limit: Int = 20): List<TaxRuleRevision> {
        val sql = "SELECT * FROM tax_rule_revisions ORDER BY created_at DESC LIMIT :limit"
        return jdbcTemplate.query(sql, MapSqlParameterSource("limit", limit)) { rs, _ ->
            TaxRuleRevision(
                id = rs.getObject("id", UUID::class.java),
                ruleId = rs.getObject("rule_id", UUID::class.java),
                scrapeId = rs.getObject("scrape_id", UUID::class.java),
                changeType = RevisionChangeType.valueOf(rs.getString("change_type")),
                oldRate = rs.getBigDecimal("old_rate"),
                newRate = rs.getBigDecimal("new_rate"),
                oldThreshold = rs.getBigDecimal("old_threshold"),
                newThreshold = rs.getBigDecimal("new_threshold"),
                rationale = rs.getString("rationale"),
                effectiveFrom = rs.getDate("effective_from").toLocalDate(),
                createdAt = rs.getObject("created_at", OffsetDateTime::class.java)
            )
        }
    }
}

@Repository
class TaxPeriodRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun createPeriod(period: TaxPeriod): TaxPeriod {
        val sql = """
            INSERT INTO tax_periods (id, organization_id, period_name, start_date, end_date, status, filing_deadline, created_at)
            VALUES (:id, :orgId, :periodName, :startDate, :endDate, :status, :deadline, :createdAt)
        """.trimIndent()
        jdbcTemplate.update(
            sql,
            MapSqlParameterSource()
                .addValue("id", period.id)
                .addValue("orgId", period.organizationId)
                .addValue("periodName", period.periodName)
                .addValue("startDate", period.startDate)
                .addValue("endDate", period.endDate)
                .addValue("status", period.status.name)
                .addValue("deadline", period.filingDeadline)
                .addValue("createdAt", period.createdAt)
        )
        return period
    }

    fun findByOrgAndPeriod(orgId: UUID, periodName: String): TaxPeriod? {
        val sql = "SELECT * FROM tax_periods WHERE organization_id = :orgId AND period_name = :periodName"
        return jdbcTemplate.query(sql, MapSqlParameterSource("orgId", orgId).addValue("periodName", periodName)) { rs, _ ->
            TaxPeriod(
                id = rs.getObject("id", UUID::class.java),
                organizationId = rs.getObject("organization_id", UUID::class.java),
                periodName = rs.getString("period_name"),
                startDate = rs.getDate("start_date").toLocalDate(),
                endDate = rs.getDate("end_date").toLocalDate(),
                status = TaxPeriodStatus.valueOf(rs.getString("status")),
                filingDeadline = rs.getDate("filing_deadline").toLocalDate(),
                createdAt = rs.getObject("created_at", OffsetDateTime::class.java)
            )
        }.firstOrNull()
    }

    fun saveSummary(summary: TaxReturnSummary): TaxReturnSummary {
        val sql = """
            INSERT INTO tax_return_summaries (
                id, organization_id, period_id, total_taxable_sales, total_exempt_sales,
                output_vat_collected, total_taxable_purchases, total_exempt_purchases,
                input_vat_paid, net_vat_payable, total_tds_withheld, status, notes, generated_at
            ) VALUES (
                :id, :orgId, :periodId, :taxableSales, :exemptSales,
                :outputVat, :taxablePurchases, :exemptPurchases,
                :inputVat, :netVat, :tdsWithheld, :status, :notes, :generatedAt
            )
            ON CONFLICT (id) DO UPDATE SET
                total_taxable_sales = EXCLUDED.total_taxable_sales,
                output_vat_collected = EXCLUDED.output_vat_collected,
                input_vat_paid = EXCLUDED.input_vat_paid,
                net_vat_payable = EXCLUDED.net_vat_payable,
                total_tds_withheld = EXCLUDED.total_tds_withheld,
                generated_at = NOW()
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            MapSqlParameterSource()
                .addValue("id", summary.id)
                .addValue("orgId", summary.organizationId)
                .addValue("periodId", summary.periodId)
                .addValue("taxableSales", summary.totalTaxableSales)
                .addValue("exemptSales", summary.totalExemptSales)
                .addValue("outputVat", summary.outputVatCollected)
                .addValue("taxablePurchases", summary.totalTaxablePurchases)
                .addValue("exemptPurchases", summary.totalExemptPurchases)
                .addValue("inputVat", summary.inputVatPaid)
                .addValue("netVat", summary.netVatPayable)
                .addValue("tdsWithheld", summary.totalTdsWithheld)
                .addValue("status", summary.status.name)
                .addValue("notes", summary.notes)
                .addValue("generatedAt", summary.generatedAt)
        )
        return summary
    }
}

@Repository
class TaxOutboxRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val objectMapper: ObjectMapper
) {
    fun save(
        eventId: UUID,
        eventType: String,
        aggregateType: String,
        aggregateId: UUID,
        tenantId: UUID?,
        routingKey: String,
        payload: Any
    ) {
        val sql = """
            INSERT INTO tax_outbox (event_id, event_type, aggregate_type, aggregate_id, tenant_id, routing_key, payload)
            VALUES (:eventId, :eventType, :aggregateType, :aggregateId, :tenantId, :routingKey, :payload)
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("eventId", eventId)
            .addValue("eventType", eventType)
            .addValue("aggregateType", aggregateType)
            .addValue("aggregateId", aggregateId)
            .addValue("tenantId", tenantId)
            .addValue("routingKey", routingKey)
            .addValue("payload", objectMapper.writeValueAsString(payload))

        jdbcTemplate.update(sql, params)
    }

    fun findUnpublished(): List<Map<String, Any>> {
        val sql = "SELECT * FROM tax_outbox WHERE published_at IS NULL ORDER BY created_at ASC LIMIT 50"
        return jdbcTemplate.queryForList(sql, MapSqlParameterSource())
    }

    fun markPublished(id: UUID) {
        val sql = "UPDATE tax_outbox SET published_at = NOW() WHERE id = :id"
        jdbcTemplate.update(sql, MapSqlParameterSource("id", id))
    }
}
