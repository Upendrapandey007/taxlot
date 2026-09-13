package com.taxlot.auth

import com.taxlot.auth.api.dto.LoginRequest
import com.taxlot.auth.api.dto.RefreshRequest
import com.taxlot.auth.api.dto.RegisterRequest
import com.taxlot.auth.api.dto.TokenResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthIntegrationTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("taxlot_auth")
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
            registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername)
            registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword)
            
            registry.add("taxlot.jwt.secret") { "this-is-a-test-secret-key-that-is-long-enough" }
        }
    }

    @Test
    fun `POST register returns 201 with tokens and POST login works`() {
        // Register
        val registerRequest = RegisterRequest("test@taxlot.com", "Password123!", "Test User")
        val registerResponse = restTemplate.postForEntity(
            "/api/v1/auth/register",
            registerRequest,
            TokenResponse::class.java
        )

        assertEquals(HttpStatus.CREATED, registerResponse.statusCode)
        assertNotNull(registerResponse.body?.accessToken)
        assertNotNull(registerResponse.body?.refreshToken)

        // Login
        val loginRequest = LoginRequest("test@taxlot.com", "Password123!")
        val loginResponse = restTemplate.postForEntity(
            "/api/v1/auth/login",
            loginRequest,
            TokenResponse::class.java
        )

        assertEquals(HttpStatus.OK, loginResponse.statusCode)
        assertNotNull(loginResponse.body?.accessToken)

        // Refresh
        val refreshRequest = RefreshRequest(loginResponse.body!!.refreshToken)
        val refreshResponse = restTemplate.postForEntity(
            "/api/v1/auth/refresh",
            refreshRequest,
            TokenResponse::class.java
        )
        
        assertEquals(HttpStatus.OK, refreshResponse.statusCode)
        assertNotNull(refreshResponse.body?.accessToken)
        assertNotEquals(loginResponse.body!!.accessToken, refreshResponse.body?.accessToken)
    }
}
