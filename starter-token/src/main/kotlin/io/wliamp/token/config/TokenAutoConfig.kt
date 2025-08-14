package io.wliamp.token.config

import io.wliamp.token.handler.InternalToken
import io.wliamp.token.handler.ThirdPartyToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(TokenProperties::class)
class TokenAutoConfig(private val props: TokenProperties,
                      @Value("\${spring.application.name}") private val applicationName: String) {

    @Bean
    fun thirdParty(): ThirdPartyToken {
        return ThirdPartyToken(props, WebClient.builder().build())
    }

    @Bean
    @ConditionalOnMissingBean
    fun internal(jwtEncoder: JwtEncoder, jwtDecoder: JwtDecoder): InternalToken {
        return InternalToken(
            jwtEncoder = jwtEncoder,
            jwtDecoder = jwtDecoder,
            defaultExpireSeconds = props.expireSeconds,
            defaultClaims = props.defaultClaims,
            applicationName = applicationName
        )
    }

}

