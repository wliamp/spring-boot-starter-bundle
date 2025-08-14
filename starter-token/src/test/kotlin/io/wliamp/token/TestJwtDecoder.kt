package io.wliamp.token

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import java.time.Instant
import java.util.Base64

/** A simple in-memory JwtDecoder for testing */
class TestJwtDecoder : JwtDecoder {

    override fun decode(token: String): Jwt {
        try {
            // Decode Base64 token into simple "key=value,key=value" string
            val decoded = String(Base64.getDecoder().decode(token))

            // Parse into Map<String, Any>
            val claims: Map<String, Any> = decoded.split(",")
                .mapNotNull { part ->
                    val kv = part.split("=", limit = 2)
                    if (kv.size == 2) kv[0] to kv[1] else null
                }.toMap()

            val now = Instant.now()

            // Build Jwt using Spring Security 6 builder
            return Jwt.withTokenValue(token)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .headers { h -> h["alg"] = "HS256" }
                .claims { c -> c.putAll(claims) }
                .build()
        } catch (e: Exception) {
            throw JwtException("Failed to decode JWT", e)
        }
    }
}
