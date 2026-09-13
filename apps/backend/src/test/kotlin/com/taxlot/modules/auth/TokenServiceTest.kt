package com.taxlot.modules.auth

import com.taxlot.config.JwtProperties
import com.taxlot.modules.auth.token.TokenService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.UUID

class TokenServiceTest : DescribeSpec({
    val jwtProps = JwtProperties(
        secret = "this-is-a-very-long-and-secure-test-secret-key-that-is-at-least-256-bits-long",
        accessExpiryMinutes = 15,
        refreshExpiryDays = 30
    )
    val tokenService = TokenService(jwtProps)

    describe("TokenService") {
        it("should generate and validate access token") {
            val userId = UUID.randomUUID()
            val token = tokenService.generateAccessToken(userId, "test@test.com", emptyList())
            token.shouldNotBeNull()

            val claims = tokenService.validateAccessToken(token)
            claims.shouldNotBeNull()
            claims.subject shouldBe userId.toString()
            claims["email"] shouldBe "test@test.com"
        }

        it("should deterministically hash tokens") {
            val token = "some-random-token-string"
            val hash1 = tokenService.hashToken(token)
            val hash2 = tokenService.hashToken(token)
            hash1 shouldBe hash2
        }

        it("should generate random refresh tokens") {
            val t1 = tokenService.generateRefreshToken()
            val t2 = tokenService.generateRefreshToken()
            t1 shouldNotBe t2
            t1.length shouldBe 128 // 64 bytes in hex
        }

        it("expired token fails validation") {
            val shortProps = JwtProperties(jwtProps.secret, 0, 30) // 0 minutes expiry
            val ts = TokenService(shortProps)
            val token = ts.generateAccessToken(UUID.randomUUID(), "test@test.com", emptyList())
            
            Thread.sleep(1000) // wait for expiry

            val claims = ts.validateAccessToken(token)
            claims.shouldBeNull()
        }
    }
})
