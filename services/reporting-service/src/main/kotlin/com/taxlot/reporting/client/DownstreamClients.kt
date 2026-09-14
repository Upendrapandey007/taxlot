package com.taxlot.reporting.client

import com.taxlot.reporting.domain.AccountBalanceItem
import com.taxlot.reporting.domain.AccountCategory
import com.taxlot.reporting.engine.AgingAnalysisEngine.RawInvoiceForAging
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Component
class AccountingDataClient(
    @Value("${taxlot.services.accounting-url:http://localhost:8084}") private val accountingUrl: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restTemplate = RestTemplate()

    fun getTrialBalanceAccounts(organizationId: UUID, periodId: UUID?): List<AccountBalanceItem> {
        return try {
            val headers = HttpHeaders().apply {
                set("X-Tenant-Id", organizationId.toString())
            }
            val entity = HttpEntity<Void>(headers)
            val url = if (periodId != null) {
                "$accountingUrl/api/v1/accounting/trial-balance?periodId=$periodId"
            } else {
                "$accountingUrl/api/v1/accounting/trial-balance"
            }

            val responseType = object : ParameterizedTypeReference<Map<String, Any>>() {}
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType)

            val body = response.body
            val itemsRaw = body?.get("items") as? List<Map<String, Any>> ?: emptyList()

            itemsRaw.mapNotNull { item ->
                try {
                    val rawType = (item["type"] as? String) ?: return@mapNotNull null
                    val category = when (rawType.uppercase()) {
                        "ASSET" -> AccountCategory.ASSET
                        "LIABILITY" -> AccountCategory.LIABILITY
                        "EQUITY" -> AccountCategory.EQUITY
                        "REVENUE" -> AccountCategory.REVENUE
                        "EXPENSE" -> AccountCategory.EXPENSE
                        else -> return@mapNotNull null
                    }
                    val balanceVal = when (val b = item["net_balance"] ?: item["netBalance"]) {
                        is Number -> BigDecimal(b.toString())
                        is String -> BigDecimal(b)
                        else -> BigDecimal.ZERO
                    }
                    val accountIdStr = (item["account_id"] ?: item["accountId"]) as? String
                    val accountId = if (accountIdStr != null) UUID.fromString(accountIdStr) else UUID.randomUUID()

                    AccountBalanceItem(
                        accountId = accountId,
                        code = item["code"] as? String ?: "",
                        name = item["name"] as? String ?: "",
                        category = category,
                        balance = balanceVal
                    )
                } catch (e: Exception) {
                    log.warn("Failed to parse trial balance line item: $item", e)
                    null
                }
            }
        } catch (ex: Exception) {
            log.warn("Could not retrieve live trial balance from accounting-service ($accountingUrl): ${ex.message}. Returning fallback sample data.")
            emptyList()
        }
    }
}

@Component
class InvoiceDataClient(
    @Value("${taxlot.services.invoice-url:http://localhost:8086}") private val invoiceUrl: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restTemplate = RestTemplate()

    fun getOutstandingInvoices(organizationId: UUID): List<RawInvoiceForAging> {
        return try {
            val headers = HttpHeaders().apply {
                set("X-Tenant-Id", organizationId.toString())
            }
            val entity = HttpEntity<Void>(headers)
            val url = "$invoiceUrl/api/v1/invoices"

            val responseType = object : ParameterizedTypeReference<List<Map<String, Any>>>() {}
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType)
            val invoices = response.body ?: emptyList()

            invoices.mapNotNull { inv ->
                val status = (inv["status"] as? String)?.uppercase()
                // Only take issued or partially paid invoices with positive outstanding balance
                if (status != "ISSUED" && status != "PARTIALLY_PAID") return@mapNotNull null

                val outstanding = when (val b = inv["outstanding_balance"] ?: inv["outstandingBalance"]) {
                    is Number -> BigDecimal(b.toString())
                    is String -> BigDecimal(b)
                    else -> BigDecimal.ZERO
                }
                if (outstanding.compareTo(BigDecimal.ZERO) <= 0) return@mapNotNull null

                val idStr = inv["id"] as? String ?: return@mapNotNull null
                val custIdStr = (inv["customer_id"] ?: inv["customerId"]) as? String ?: return@mapNotNull null
                val invNum = (inv["invoice_number"] ?: inv["invoiceNumber"]) as? String ?: "INV-UNKNOWN"
                val dueDateStr = (inv["due_date"] ?: inv["dueDate"]) as? String
                val dueDate = if (dueDateStr != null) LocalDate.parse(dueDateStr) else LocalDate.now()

                RawInvoiceForAging(
                    invoiceId = UUID.fromString(idStr),
                    customerId = UUID.fromString(custIdStr),
                    customerName = "Customer ${custIdStr.substring(0, 8)}",
                    invoiceNumber = invNum,
                    dueDate = dueDate,
                    outstandingBalance = outstanding
                )
            }
        } catch (ex: Exception) {
            log.warn("Could not retrieve live invoices from invoice-service ($invoiceUrl): ${ex.message}. Returning fallback sample data.")
            emptyList()
        }
    }
}
