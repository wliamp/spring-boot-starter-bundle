package io.github.wliamp.token.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import io.github.wliamp.token.data.FacebookParty
import io.github.wliamp.token.data.GoogleParty
import io.github.wliamp.token.data.OauthParty
import io.github.wliamp.token.data.ZaloParty
import io.github.wliamp.token.util.ExternalToken
import io.github.wliamp.token.util.InternalToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.web.reactive.function.client.WebClient
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
    fun reactiveJwtDecoder(rsaKey: RSAKey): ReactiveJwtDecoder = NimbusReactiveJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build()

    @Bean
    @ConditionalOnProperty(prefix = "token.facebook", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun fb(): OauthParty = FacebookParty(props, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(prefix = "token.google", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun gg(): OauthParty = GoogleParty(props, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(prefix = "token.zalo", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun zl(): OauthParty = ZaloParty(props, WebClient.builder().build())

    @Bean
    @ConditionalOnMissingBean
    fun external(
        fb: OauthParty,
        gg: OauthParty,
        zl: OauthParty
    ): ExternalToken = ExternalToken(fb, gg, zl)

    @Bean
    @ConditionalOnMissingBean
    fun internal(
        jwtEncoder: JwtEncoder, jwtDecoder: ReactiveJwtDecoder
    ): InternalToken = InternalToken(
        jwtEncoder = jwtEncoder,
        jwtDecoder = jwtDecoder,
        defaultExpireSeconds = props.expireSeconds,
        defaultClaims = props.defaultClaims,
        applicationName = applicationName
    )
}

