package com.taxlot.tax

import com.taxlot.tax.domain.TaxRule
import com.taxlot.tax.engine.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class TaxCalculationEngineTest : DescribeSpec({

    val jurisdictionId = UUID.randomUUID()
    val activeRules = listOf(
        TaxRule(
            jurisdictionId = jurisdictionId,
            categoryCode = "VAT_STANDARD",
            name = "Standard VAT 13%",
            rate = BigDecimal("0.1300"),
            legalReference = "Value Added Tax Act 2052, Section 7",
            description = null,
            effectiveFrom = LocalDate.of(2020, 1, 1)
        ),
        TaxRule(
            jurisdictionId = jurisdictionId,
            categoryCode = "VAT_ZERO",
            name = "Export Zero-Rated",
            rate = BigDecimal("0.0000"),
            legalReference = "Value Added Tax Act 2052, Section 7(2)",
            description = null,
            effectiveFrom = LocalDate.of(2020, 1, 1)
        ),
        TaxRule(
            jurisdictionId = jurisdictionId,
            categoryCode = "VAT_EXEMPT",
            name = "Exempt Goods",
            rate = BigDecimal("0.0000"),
            isExempt = true,
            legalReference = "Value Added Tax Act 2052, Schedule 1",
            description = null,
            effectiveFrom = LocalDate.of(2020, 1, 1)
        ),
        TaxRule(
            jurisdictionId = jurisdictionId,
            categoryCode = "TDS_RENT",
            name = "TDS Rent 10%",
            rate = BigDecimal("0.1000"),
            legalReference = "Income Tax Act 2058, Section 88(1)",
            description = null,
            effectiveFrom = LocalDate.of(2020, 1, 1)
        ),
        TaxRule(
            jurisdictionId = jurisdictionId,
            categoryCode = "TDS_CONTRACT",
            name = "TDS Contract 1.5%",
            rate = BigDecimal("0.0150"),
            thresholdAmount = BigDecimal("50000.0000"),
            legalReference = "Income Tax Act 2058, Section 89(1)",
            description = null,
            effectiveFrom = LocalDate.of(2020, 1, 1)
        )
    )

    val engine = TaxCalculationEngine()

    describe("CA Tax Calculation Engine") {
        it("correctly calculates Standard 13% VAT on taxable goods") {
            val req = TaxCalculationRequest(
                countryCode = "NP",
                items = listOf(
                    TaxLineItemRequest(
                        lineNumber = 1,
                        description = "Accounting Software License",
                        quantity = BigDecimal("2"),
                        unitPrice = BigDecimal("1000.0000"),
                        categoryCode = "VAT_STANDARD"
                    )
                )
            )
            val res = engine.calculateTax(req, activeRules)
            res.subtotal shouldBe BigDecimal("2000.0000")
            res.totalTaxableAmount shouldBe BigDecimal("2000.0000")
            res.totalVatAmount shouldBe BigDecimal("260.0000")
            res.grandTotal shouldBe BigDecimal("2260.0000")
            res.netReceivableOrPayable shouldBe BigDecimal("2260.0000")
        }

        it("handles export zero-rated sales correctly") {
            val req = TaxCalculationRequest(
                countryCode = "NP",
                items = listOf(
                    TaxLineItemRequest(
                        lineNumber = 1,
                        description = "Software Export to US Client",
                        quantity = BigDecimal("1"),
                        unitPrice = BigDecimal("50000.0000"),
                        isExport = true
                    )
                )
            )
            val res = engine.calculateTax(req, activeRules)
            res.totalVatAmount shouldBe BigDecimal("0.0000")
            res.lines[0].isZeroRated shouldBe true
            res.lines[0].legalReference shouldBe "Value Added Tax Act 2052, Section 7(2)"
        }

        it("handles Schedule 1 VAT Exempt supplies") {
            val req = TaxCalculationRequest(
                countryCode = "NP",
                items = listOf(
                    TaxLineItemRequest(
                        lineNumber = 1,
                        description = "Agricultural Seed Supplies",
                        quantity = BigDecimal("10"),
                        unitPrice = BigDecimal("500.0000"),
                        isExempt = true
                    )
                )
            )
            val res = engine.calculateTax(req, activeRules)
            res.totalExemptAmount shouldBe BigDecimal("5000.0000")
            res.totalTaxableAmount shouldBe BigDecimal("0.0000")
            res.totalVatAmount shouldBe BigDecimal("0.0000")
            res.lines[0].isExempt shouldBe true
        }

        it("calculates TDS on rent withholding") {
            val req = TaxCalculationRequest(
                countryCode = "NP",
                items = listOf(
                    TaxLineItemRequest(
                        lineNumber = 1,
                        description = "Office Space Rent",
                        quantity = BigDecimal("1"),
                        unitPrice = BigDecimal("40000.0000"),
                        categoryCode = "TDS_RENT"
                    )
                )
            )
            val res = engine.calculateTax(req, activeRules)
            // TDS Rent: 10% of 40,000 = 4,000
            res.totalTdsWithheld shouldBe BigDecimal("4000.0000")
            res.netReceivableOrPayable shouldBe BigDecimal("36000.0000")
        }

        it("applies TDS contract withholding only if above threshold") {
            // Below threshold: 30,000 < 50,000 -> 0 TDS
            val belowReq = TaxCalculationRequest(
                countryCode = "NP",
                items = listOf(
                    TaxLineItemRequest(
                        lineNumber = 1,
                        description = "Minor Repair Work",
                        unitPrice = BigDecimal("30000.0000"),
                        categoryCode = "TDS_CONTRACT"
                    )
                )
            )
            val belowRes = engine.calculateTax(belowReq, activeRules)
            belowRes.totalTdsWithheld shouldBe BigDecimal("0.0000")

            // Above threshold: 60,000 >= 50,000 -> 1.5% TDS = 900
            val aboveReq = TaxCalculationRequest(
                countryCode = "NP",
                items = listOf(
                    TaxLineItemRequest(
                        lineNumber = 1,
                        description = "Office Renovation Contract",
                        unitPrice = BigDecimal("60000.0000"),
                        categoryCode = "TDS_CONTRACT"
                    )
                )
            )
            val aboveRes = engine.calculateTax(aboveReq, activeRules)
            aboveRes.totalTdsWithheld shouldBe BigDecimal("900.0000")
        }
    }
})
