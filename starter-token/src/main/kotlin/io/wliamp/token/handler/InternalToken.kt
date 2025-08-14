package io.wliamp.token.handler

import io.wliamp.token.data.Token
import io.wliamp.token.data.Type
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class InternalToken(
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: JwtDecoder,
    @Value("\${token.default-expire-seconds:3600}") private val defaultExpireSeconds: Long,
    @Value("\${token.default-claims:}") private val defaultClaims: Map<String, Any>,
    @Value("\${spring.application.name}") private val applicationName: String
) {
    private val issuer = applicationName
    private val defaultClaimsWithApp = defaultClaims + mapOf("app" to issuer)

    fun issue(subject: String, type: Type = Type.ACCESS, extraClaims: Map<String, Any> = emptyMap()): String {
        val now = Instant.now()
        val claimsBuilder = JwtClaimsSet.builder()
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(defaultExpireSeconds))
            .subject(subject)
            .claim("type", type.name)
            .claim("iat", now.epochSecond)
            .claim("exp", now.plusSeconds(defaultExpireSeconds).epochSecond)

        defaultClaimsWithApp.forEach { (k, v) -> claimsBuilder.claim(k, v) }
        extraClaims.forEach { (k, v) -> claimsBuilder.claim(k, v) }

        return jwtEncoder.encode(JwtEncoderParameters.from(claimsBuilder.build())).tokenValue
    }

    /** Decode token */
    fun getClaims(token: String): Map<String, Any> {
        val jwt: Jwt = jwtDecoder.decode(token)
        return jwt.claims
    }

    /** Check if token expired */
    fun isExpired(token: String): Boolean {
        val jwt: Jwt = jwtDecoder.decode(token)
        return jwt.expiresAt?.isBefore(Instant.now()) ?: true
    }

    /** Verify token */
    fun verify(token: String): Boolean {
        return try {
            jwtDecoder.decode(token)
            true
        } catch (ex: Exception) {
            false
        }
    }

    /** Get token type */
    fun getType(token: String): Type {
        val claims = getClaims(token)
        return Type.valueOf(claims["type"]?.toString() ?: Type.ACCESS.name)
    }

    /** Retrieve summarized information about the token */
    fun tokenInfo(token: String): Token {
        val jwt = jwtDecoder.decode(token)
        val claims = jwt.claims
        val iat = jwt.issuedAt ?: Instant.EPOCH
        val exp = jwt.expiresAt ?: Instant.EPOCH
        return Token(
            subject = claims["sub"]?.toString() ?: "",
            type = getType(token),
            issuedAt = iat,
            expiration = exp,
            claims = claims
        )
    }

    /** Validate that the token subject matches the expected value */
    fun validateSubject(token: String, expected: String): Boolean {
        return getClaims(token)["sub"] == expected
    }

    /** Validate a specific claim in the token */
    fun validateClaim(token: String, key: String, expected: String): Boolean {
        return getClaims(token)[key] == expected
    }

    /** Refresh the token: retain existing claims and type, generate new iat/exp */
    fun refresh(token: String): String {
        val oldClaims = getClaims(token).toMutableMap()
        val subject = oldClaims["sub"]?.toString() ?: throw IllegalArgumentException("Invalid token")
        val type = getType(token)

        // Remove JWT time claims, new issue() sẽ tạo iat/exp mới
        oldClaims.remove("iat")
        oldClaims.remove("exp")

        // Giữ lại các claims còn lại
        val extraClaims = oldClaims.filterKeys { it != "sub" && it != "type" }

        return issue(subject, type, extraClaims)
    }
}
