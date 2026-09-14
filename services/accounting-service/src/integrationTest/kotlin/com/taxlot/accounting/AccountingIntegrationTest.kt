package com.taxlot.accounting

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.accounting.api.PostJournalEntryRequest
import com.taxlot.accounting.application.InitializeDefaultAccountsUseCase
import com.taxlot.accounting.domain.EntrySourceType
import com.taxlot.accounting.domain.JournalEntry
import com.taxlot.accounting.domain.JournalLine
import com.taxlot.accounting.domain.PeriodStatus
import com.taxlot.accounting.infrastructure.AccountRepository
import com.taxlot.accounting.infrastructure.AccountingPeriodRepository
import com.taxlot.accounting.infrastructure.JournalEntryRepository
import com.taxlot.accounting.infrastructure.LedgerBalanceRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AccountingIntegrationTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("accounting")
            withUsername("test")
            withPassword("test")
        }

        @Container
        val rabbitmq = RabbitMQContainer("rabbitmq:3-management-alpine")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.rabbitmq.host", rabbitmq::getHost)
            registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort)
        }
    }

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var initializeDefaultAccountsUseCase: InitializeDefaultAccountsUseCase
    @Autowired lateinit var accountRepository: AccountRepository
    @Autowired lateinit var accountingPeriodRepository: AccountingPeriodRepository
    @Autowired lateinit var ledgerBalanceRepository: LedgerBalanceRepository

    val organizationId = UUID.randomUUID()
    
    @BeforeEach
    fun setup() {
        if (accountRepository.listByOrganization(organizationId).isEmpty()) {
            initializeDefaultAccountsUseCase.execute(organizationId)
        }
    }

    @Test
    fun `post balanced journal entry and verify DB, ledger and reverse it`() {
        val cashAccount = accountRepository.findByCode(organizationId, "1010")!!
        val salesAccount = accountRepository.findByCode(organizationId, "4010")!!

        val lines = listOf(
            JournalLine(organizationId = organizationId, accountId = cashAccount.id, lineNumber = 1, debit = BigDecimal("500.00")),
            JournalLine(organizationId = organizationId, accountId = salesAccount.id, lineNumber = 2, credit = BigDecimal("500.00"))
        )

        val request = PostJournalEntryRequest(
            entryDate = LocalDate.now(),
            description = "Test Sale",
            reference = "INV-001",
            sourceType = EntrySourceType.MANUAL,
            lines = lines
        )

        // Post Entry
        val responseJson = mockMvc.perform(post("/api/v1/accounting/journal-entries")
            .header("X-Tenant-Id", organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString

        val entry = objectMapper.readValue(responseJson, JournalEntry::class.java)
        assertNotNull(entry.id)

        // Verify Ledger
        val period = accountingPeriodRepository.findPeriodForDate(organizationId, LocalDate.now())!!
        val trialBalance = ledgerBalanceRepository.getTrialBalance(organizationId, period.id)
        
        val cashBalance = trialBalance.find { it["code"] == "1010" }!!["net_balance"] as BigDecimal
        val salesBalance = trialBalance.find { it["code"] == "4010" }!!["net_balance"] as BigDecimal
        
        assertEquals(0, cashBalance.compareTo(BigDecimal("500.00")))
        assertEquals(0, salesBalance.compareTo(BigDecimal("-500.00")))

        // Reverse Entry
        val reverseResponseJson = mockMvc.perform(post("/api/v1/accounting/journal-entries/${entry.id}/reverse")
            .header("X-Tenant-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val reversedEntry = objectMapper.readValue(reverseResponseJson, JournalEntry::class.java)
        assertNotNull(reversedEntry.id)
        
        // Ledger should be back to 0 for this entry
        val trialBalanceAfterReverse = ledgerBalanceRepository.getTrialBalance(organizationId, period.id)
        val cashBalanceAfter = trialBalanceAfterReverse.find { it["code"] == "1010" }!!["net_balance"] as BigDecimal
        assertEquals(0, cashBalanceAfter.compareTo(BigDecimal.ZERO))
    }

    @Test
    fun `unbalanced entry fails with 422`() {
        val cashAccount = accountRepository.findByCode(organizationId, "1010")!!
        val salesAccount = accountRepository.findByCode(organizationId, "4010")!!

        val lines = listOf(
            JournalLine(organizationId = organizationId, accountId = cashAccount.id, lineNumber = 1, debit = BigDecimal("500.00")),
            JournalLine(organizationId = organizationId, accountId = salesAccount.id, lineNumber = 2, credit = BigDecimal("400.00")) // Unbalanced
        )

        val request = PostJournalEntryRequest(
            entryDate = LocalDate.now(),
            description = "Test Unbalanced",
            reference = "INV-002",
            sourceType = EntrySourceType.MANUAL,
            lines = lines
        )

        mockMvc.perform(post("/api/v1/accounting/journal-entries")
            .header("X-Tenant-Id", organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `posting to locked period fails with 422`() {
        val cashAccount = accountRepository.findByCode(organizationId, "1010")!!
        val salesAccount = accountRepository.findByCode(organizationId, "4010")!!

        // Lock period
        val period = accountingPeriodRepository.findPeriodForDate(organizationId, LocalDate.now())!!
        accountingPeriodRepository.updateStatus(period.id, organizationId, PeriodStatus.LOCKED, null)

        val lines = listOf(
            JournalLine(organizationId = organizationId, accountId = cashAccount.id, lineNumber = 1, debit = BigDecimal("500.00")),
            JournalLine(organizationId = organizationId, accountId = salesAccount.id, lineNumber = 2, credit = BigDecimal("500.00"))
        )

        val request = PostJournalEntryRequest(
            entryDate = LocalDate.now(),
            description = "Test Locked",
            reference = "INV-003",
            sourceType = EntrySourceType.MANUAL,
            lines = lines
        )

        mockMvc.perform(post("/api/v1/accounting/journal-entries")
            .header("X-Tenant-Id", organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity)

        // Unlock for other tests
        accountingPeriodRepository.updateStatus(period.id, organizationId, PeriodStatus.OPEN, null)
    }
}
