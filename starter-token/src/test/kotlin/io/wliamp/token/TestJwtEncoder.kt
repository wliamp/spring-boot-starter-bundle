package io.wliamp.token

import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.JwtEncodingException
import java.util.Base64

/** A simple in-memory JwtEncoder for testing */
class TestJwtEncoder : JwtEncoder {

    private val secret = "test-secret-key" // in-memory secret for HMAC

    override fun encode(parameters: JwtEncoderParameters): org.springframework.security.oauth2.jwt.Jwt {
        try {
            val claims = parameters.claims.claims
            // Simple token: base64 encode claims map as string
            val payload = claims.entries.joinToString(",") { "${it.key}=${it.value}" }
            val tokenValue = Base64.getEncoder().encodeToString(payload.toByteArray())
            val now = java.time.Instant.now()
            return org.springframework.security.oauth2.jwt.Jwt(
                tokenValue,
                now,
                now.plusSeconds(3600),
                mapOf("alg" to "HS256"),
                claims
            )
        } catch (e: Exception) {
            throw JwtEncodingException("Failed to encode JWT", e)
        }
    }
}
