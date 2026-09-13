package com.taxlot.modules.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.modules.auth.dto.LoginRequest
import com.taxlot.modules.auth.dto.RefreshRequest
import com.taxlot.modules.auth.dto.RegisterRequest
import com.taxlot.modules.auth.dto.TokenResponse
import org.junit.jupiter.api.Assertions.*
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
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("taxlot_test")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Test
    fun `register creates user and returns tokens`() {
        val request = RegisterRequest(
            email = "test_${UUID.randomUUID()}@example.com",
            password = "Password123!",
            fullName = "Test User"
        )

        val result = mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(result.response.contentAsString, TokenResponse::class.java)
        assertNotNull(response.accessToken)
        assertNotNull(response.refreshToken)
    }

    @Test
    fun `login with valid credentials returns tokens`() {
        val email = "login_${UUID.randomUUID()}@example.com"
        val password = "Password123!"
        
        // Register first
        mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(RegisterRequest(email, password, "Test User"))))

        // Login
        val result = mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(LoginRequest(email, password))))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(result.response.contentAsString, TokenResponse::class.java)
        assertNotNull(response.accessToken)
    }

    @Test
    fun `login with wrong password returns 401`() {
        val email = "wrongpass_${UUID.randomUUID()}@example.com"
        
        mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(RegisterRequest(email, "Password123!", "Test User"))))

        mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(LoginRequest(email, "WrongPassword!"))))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `register with duplicate email returns 409`() {
        val email = "dup_${UUID.randomUUID()}@example.com"
        val req = RegisterRequest(email, "Password123!", "Test User")
        
        mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk)

        mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isConflict)
    }

    @Test
    fun `refresh rotates refresh token`() {
        val email = "refresh_${UUID.randomUUID()}@example.com"
        val regReq = RegisterRequest(email, "Password123!", "Test User")
        
        val regRes = mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(regReq)))
            .andReturn()

        val tokens = objectMapper.readValue(regRes.response.contentAsString, TokenResponse::class.java)

        val refReq = RefreshRequest(tokens.refreshToken)
        val refRes = mockMvc.perform(post("/api/v1/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(refReq)))
            .andExpect(status().isOk)
            .andReturn()

        val newTokens = objectMapper.readValue(refRes.response.contentAsString, TokenResponse::class.java)
        assertNotEquals(tokens.accessToken, newTokens.accessToken)
        assertNotEquals(tokens.refreshToken, newTokens.refreshToken)
    }

    @Test
    fun `logout invalidates refresh token`() {
        val email = "logout_${UUID.randomUUID()}@example.com"
        val regReq = RegisterRequest(email, "Password123!", "Test User")
        
        val regRes = mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(regReq)))
            .andReturn()

        val tokens = objectMapper.readValue(regRes.response.contentAsString, TokenResponse::class.java)

        mockMvc.perform(post("/api/v1/auth/logout")
            .header("Authorization", "Bearer ${tokens.accessToken}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(RefreshRequest(tokens.refreshToken))))
            .andExpect(status().isOk)

        // Try to refresh with invalidated token
        mockMvc.perform(post("/api/v1/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(RefreshRequest(tokens.refreshToken))))
            .andExpect(status().isUnauthorized)
    }
}
