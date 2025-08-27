package io.github.wliamp.token.util

import io.github.wliamp.token.data.Token
import io.github.wliamp.token.data.Type
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Instant

@Component
class TokenUtil(
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: ReactiveJwtDecoder,
    @Value("\${token.default-expire-seconds:3600}") private val defaultExpireSeconds: Long,
    @Value("\${token.default-claims:}") private val defaultClaims: Map<String, Any>,
    @Value("\${spring.application.name}") private val applicationName: String
) {
    private val issuer = applicationName

    private val defaultClaimsWithApp = defaultClaims + mapOf("app" to issuer)

    @JvmOverloads
    fun issue(
        subject: String,
        type: Type = Type.ACCESS,
        expiresInSeconds: Long = defaultExpireSeconds,
        extraClaims: Map<String, Any> = emptyMap()
    ): Mono<String> =
        Mono.fromCallable {
            val now = Instant.now()
            val claimsBuilder = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresInSeconds))
                .subject(subject)
                .claim("type", type.name)
            defaultClaimsWithApp.forEach { (k, v) -> claimsBuilder.claim(k, v) }
            extraClaims.forEach { (k, v) -> claimsBuilder.claim(k, v) }
            jwtEncoder.encode(JwtEncoderParameters.from(claimsBuilder.build())).tokenValue
        }.subscribeOn(Schedulers.boundedElastic())

    /** Decode token */
    fun getClaims(token: String): Mono<Map<String, Any>> =
        jwtDecoder.decode(token).map { it.claims }

    /** Check if token expired */
    fun isExpired(token: String): Mono<Boolean> =
        jwtDecoder.decode(token)
            .map { jwt -> jwt.expiresAt?.isBefore(Instant.now()) ?: true }

    /** Verify token */
    fun verify(token: String): Mono<Boolean> =
        jwtDecoder.decode(token)
            .map { true }
            .onErrorReturn(false)

    /** Get token type */
    fun getType(token: String): Mono<Type> =
        getClaims(token).map { claims ->
            Type.valueOf(claims["type"]?.toString() ?: Type.ACCESS.name)
        }

    /** Retrieve summarized information about the token */
    fun tokenInfo(token: String): Mono<Token> =
        jwtDecoder.decode(token).flatMap { jwt ->
            getType(token).map { type ->
                Token(
                    subject = jwt.claims["sub"]?.toString() ?: "",
                    type = type,
                    issuedAt = jwt.issuedAt ?: Instant.EPOCH,
                    expiration = jwt.expiresAt ?: Instant.EPOCH,
                    claims = jwt.claims
                )
            }
        }

    /** Validate that the token subject matches the expected value */
    fun validateSubject(token: String, expected: String): Mono<Boolean> =
        getClaims(token).map { it["sub"] == expected }

    /** Validate a specific claim in the token */
    fun validateClaim(token: String, key: String, expected: String): Mono<Boolean> =
        getClaims(token).map { it[key] == expected }

    /** Refresh the token: retain existing claims and type, generate new iat/exp */
    fun refresh(token: String, expiresInSeconds: Long): Mono<String> =
        getClaims(token).flatMap { oldClaims ->
            val subject = oldClaims["sub"]?.toString()
                ?: return@flatMap Mono.error<String>(IllegalArgumentException("Missing subject"))
            val typeStr = oldClaims["type"]?.toString()
                ?: return@flatMap Mono.error<String>(IllegalArgumentException("Missing type"))
            val type = Type.valueOf(typeStr)
            val preserved = oldClaims
                .filterKeys { it !in setOf("iat", "exp", "nbf", "sub", "type") }
            issue(subject, type, expiresInSeconds, preserved)
        }
}
