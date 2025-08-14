package io.wliamp.token

import io.wliamp.token.data.Type
import io.wliamp.token.handler.InternalToken
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import java.time.Instant
import java.util.Base64
import java.util.UUID

class TestInternalToken {

    private lateinit var internalToken: InternalToken
    private lateinit var encoder: JwtEncoder
    private lateinit var decoder: JwtDecoder

    @BeforeEach
    fun setup() {
        encoder = object : JwtEncoder {
            override fun encode(parameters: JwtEncoderParameters): Jwt {
                val now = Instant.now()
                val claims = parameters.claims.claims.mapValues { it.value.toString() }.toMutableMap()
                claims.putIfAbsent("iat", now.epochSecond.toString())
                claims.putIfAbsent("exp", now.plusSeconds(3600).epochSecond.toString())
                if (!claims.containsKey("_rand")) {
                    claims["_rand"] = UUID.randomUUID().toString()
                }
                val tokenStr = claims.entries.joinToString(",") { "${it.key}=${it.value}" }
                val tokenBase64 = Base64.getEncoder().encodeToString(tokenStr.toByteArray())
                return Jwt(
                    tokenBase64,
                    now,
                    now.plusSeconds(3600),
                    mapOf("alg" to "none"),
                    claims.mapValues { it.value as Any }
                )
            }
        }

        decoder = object : JwtDecoder {
            override fun decode(token: String): Jwt {
                val decodedStr = String(Base64.getDecoder().decode(token))
                val claims: Map<String, String> = decodedStr.split(",")
                    .mapNotNull {
                        val parts = it.split("=", limit = 2)
                        if (parts.size == 2) parts[0] to parts[1] else null
                    }.toMap()
                val now = Instant.now()
                val iat = claims["iat"]?.toLongOrNull() ?: now.epochSecond
                val exp = claims["exp"]?.toLongOrNull() ?: now.plusSeconds(3600).epochSecond
                val claimsAny = claims.mapValues { it.value as Any }
                return Jwt(
                    token,
                    Instant.ofEpochSecond(iat),
                    Instant.ofEpochSecond(exp),
                    mapOf("alg" to "none"),
                    claimsAny
                )
            }
        }

        internalToken = InternalToken(
            jwtEncoder = encoder,
            jwtDecoder = decoder,
            defaultExpireSeconds = 3600,
            defaultClaims = mapOf("app" to "demo")
        )
    }

    @Test
    fun testIssueAccessToken() {
        val token = internalToken.issue("user1", Type.ACCESS)
        assertNotNull(token)
        assertTrue(internalToken.verify(token))
        val claims = internalToken.getClaims(token)
        assertEquals("user1", claims["sub"])
        assertEquals("ACCESS", claims["type"])
        assertEquals("demo", claims["app"])
    }

    @Test
    fun testIssueRefreshToken() {
        val token = internalToken.issue("user1", Type.REFRESH)
        val claims = internalToken.getClaims(token)
        assertEquals("REFRESH", claims["type"])
    }

    @Test
    fun testRefreshToken() {
        val original = internalToken.issue("user2", Type.REFRESH)
        val refreshed = internalToken.refresh(original)
        assertNotEquals(original, refreshed) // token mới chắc chắn khác token cũ
        assertEquals(internalToken.getType(original), internalToken.getType(refreshed))
    }

    @Test
    fun testIsExpired() {
        println("=== testIsExpired ===")
        val validToken = internalToken.issue("user3")
        assertFalse(internalToken.isExpired(validToken))
        val now = Instant.now()
        val expiredClaims = mapOf(
            "sub" to "user3",
            "type" to "ACCESS",
            "app" to "demo",
            "iat" to (now.minusSeconds(3600).epochSecond.toString()),
            "exp" to (now.minusSeconds(10).epochSecond.toString())
        )
        val expiredToken = internalToken.issue("user3", Type.ACCESS, expiredClaims)
        assertTrue(internalToken.isExpired(expiredToken))
    }


    @Test
    fun testValidateClaims() {
        val token = internalToken.issue("user4", Type.ACCESS, mapOf("role" to "ADMIN"))
        val claims = internalToken.getClaims(token)
        assertTrue(internalToken.validateSubject(token, "user4"))
        assertTrue(internalToken.validateClaim(token, "role", "ADMIN"))
    }

    @Test
    fun testTokenInfo() {
        val token = internalToken.issue("user5", Type.SERVICE)
        val info = internalToken.tokenInfo(token)
        assertEquals("user5", info.subject)
        assertEquals(Type.SERVICE, info.type)
        assertNotNull(info.issuedAt)
        assertNotNull(info.expiration)
    }
}
