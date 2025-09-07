package io.github.wliamp.token.config

import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import io.github.wliamp.token.data.EnvSecretLoader
import io.github.wliamp.token.data.KeySetManager
import io.github.wliamp.token.data.SecretLoader
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

@AutoConfiguration
@EnableConfigurationProperties(TokenProperties::class)
class TokenAutoConfig(
    private val props: TokenProperties,
    @Value("\${spring.application.name}") private val applicationName: String
) {
    @Bean
    fun secretLoader(): SecretLoader = EnvSecretLoader(props)

    @Bean
    fun jwkSource(keySetManager: KeySetManager): JWKSource<SecurityContext> =
        keySetManager.signingJwkSource()

    @Bean
    fun jwtEncoder(jwkSource: JWKSource<SecurityContext>): JwtEncoder =
        NimbusJwtEncoder(jwkSource)

    @Bean
    fun reactiveJwtDecoder(keySetManager: KeySetManager): ReactiveJwtDecoder =
        NimbusReactiveJwtDecoder.withPublicKey(
            keySetManager.currentKeySet().active().toRSAPublicKey()
        ).build()

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

