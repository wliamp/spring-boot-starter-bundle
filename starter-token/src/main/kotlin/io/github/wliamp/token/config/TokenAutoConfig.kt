package io.github.wliamp.token.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import io.github.wliamp.token.util.TokenUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.UUID

@AutoConfiguration
@EnableConfigurationProperties(TokenProperties::class)
class TokenAutoConfig(
    private val props: TokenProperties,
    @Value("\${spring.application.name}") private val applicationName: String
) {
    @Bean
    fun rsaKey(): RSAKey =
        KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048)
        }.genKeyPair().let { keyPair ->
            RSAKey.Builder(keyPair.public as RSAPublicKey)
                .privateKey(keyPair.private as RSAPrivateKey)
                .keyID(UUID.randomUUID().toString())
                .build()
        }

    @Bean
    fun jwkSource(rsaKey: RSAKey): JWKSource<SecurityContext> = ImmutableJWKSet(JWKSet(rsaKey))

    @Bean
    fun jwtEncoder(jwkSource: JWKSource<SecurityContext>): JwtEncoder = NimbusJwtEncoder(jwkSource)

    @Bean
    fun reactiveJwtDecoder(rsaKey: RSAKey): ReactiveJwtDecoder =
        NimbusReactiveJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build()

    @Bean
    @ConditionalOnMissingBean
    fun tokenUtil(
        jwtEncoder: JwtEncoder, jwtDecoder: ReactiveJwtDecoder
    ): TokenUtil = TokenUtil(
        jwtEncoder = jwtEncoder,
        jwtDecoder = jwtDecoder,
        defaultExpireSeconds = props.expireSeconds,
        defaultClaims = props.defaultClaims,
        applicationName = applicationName
    )
}

