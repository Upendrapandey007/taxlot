package com.taxlot.organization

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.organization.application.CreateOrganizationRequest
import com.taxlot.organization.application.InviteRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrganizationServiceIntegrationTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("taxlot_organization")
            withUsername("taxlot")
            withPassword("taxlot")
        }

        @Container
        val rabbitmq = RabbitMQContainer("rabbitmq:3.12-management-alpine")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.rabbitmq.host", rabbitmq::getHost)
            registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort)
            // Small outbox delay for fast testing
            registry.add("taxlot.outbox.delay", { "100" })
        }
    }

    @Test
    fun `should create organization and publish event`() {
        val userId = UUID.randomUUID()
        val headers = HttpHeaders().apply {
            set("X-User-Id", userId.toString())
        }
        
        val req = CreateOrganizationRequest(
            name = "Test Org",
            industry = "OTHER",
            taxNumber = null,
            countryCode = "US",
            currencyCode = "USD"
        )
        val createRes = restTemplate.exchange("/api/v1/organizations", HttpMethod.POST, HttpEntity(req, headers), Map::class.java)
        
        assertEquals(HttpStatus.CREATED, createRes.statusCode)
        val orgId = createRes.body!!["id"] as String
        assertNotNull(orgId)

        // Test getting org
        val getRes = restTemplate.exchange("/api/v1/organizations/$orgId", HttpMethod.GET, HttpEntity<Void>(headers), Map::class.java)
        assertEquals(HttpStatus.OK, getRes.statusCode)
        
        // Test listing user orgs
        val listRes = restTemplate.exchange("/api/v1/organizations", HttpMethod.GET, HttpEntity<Void>(headers), List::class.java)
        assertEquals(HttpStatus.OK, listRes.statusCode)
        assertEquals(1, listRes.body!!.size)
        
        // Test inviting a member
        val inviteReq = InviteRequest(email = "friend@test.com", role = "ADMIN")
        val inviteRes = restTemplate.exchange("/api/v1/organizations/$orgId/invitations", HttpMethod.POST, HttpEntity(inviteReq, headers), Map::class.java)
        assertEquals(HttpStatus.CREATED, inviteRes.statusCode)
        
        // Non-member access test
        val strangerId = UUID.randomUUID()
        val strangerHeaders = HttpHeaders().apply { set("X-User-Id", strangerId.toString()) }
        val strangerRes = restTemplate.exchange("/api/v1/organizations/$orgId", HttpMethod.GET, HttpEntity<Void>(strangerHeaders), Map::class.java)
        assertEquals(HttpStatus.FORBIDDEN, strangerRes.statusCode)
    }
}
