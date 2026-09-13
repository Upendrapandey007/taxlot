package com.taxlot.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.platform.event.EventEnvelope
import com.taxlot.user.infrastructure.UserProfileRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
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
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserServiceIntegrationTest {

    @Autowired
    lateinit var rabbitTemplate: RabbitTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var userProfileRepository: UserProfileRepository

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("taxlot_user")
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
        }
    }

    @Test
    fun `should consume user registered event and expose profile via api`() {
        val userId = UUID.randomUUID()
        val envelope = EventEnvelope(
            eventId = UUID.randomUUID(),
            eventType = "UserRegistered",
            producer = "auth-service",
            aggregateType = "User",
            aggregateId = userId,
            occurredAt = OffsetDateTime.now(ZoneOffset.UTC),
            payload = mapOf(
                "userId" to userId.toString(),
                "email" to "test@example.com",
                "fullName" to "Test User"
            )
        )
        
        val json = objectMapper.writeValueAsString(envelope)
        rabbitTemplate.convertAndSend("user-service.user-registered", json)
        
        Thread.sleep(1000)
        
        val profile = userProfileRepository.findByUserId(userId)
        assertNotNull(profile)
        assertEquals("test@example.com", profile?.email)
        assertEquals("Test User", profile?.fullName)
        
        // Idempotency check
        rabbitTemplate.convertAndSend("user-service.user-registered", json)
        Thread.sleep(500)
        // No error should be thrown
        
        val headers = HttpHeaders().apply {
            set("X-User-Id", userId.toString())
        }
        val entity = HttpEntity<Void>(headers)
        val response = restTemplate.exchange("/api/v1/users/me", HttpMethod.GET, entity, Map::class.java)
        
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body as Map<String, Any>
        assertEquals("test@example.com", body["email"])
    }
}
