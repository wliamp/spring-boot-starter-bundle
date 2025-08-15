package io.wliamp.token.config

import io.wliamp.token.data.FacebookParty
import io.wliamp.token.data.GoogleParty
import io.wliamp.token.data.ZaloParty
import io.wliamp.token.util.InternalToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(TokenProperties::class)
class TokenAutoConfig(private val props: TokenProperties,
                      @Value("\${spring.application.name}") private val applicationName: String) {

    @Bean
    fun google(): GoogleParty {
        return GoogleParty(props, WebClient.builder().build())
    }

    @Bean
    fun facebook(): FacebookParty {
        return FacebookParty(props, WebClient.builder().build())
    }

    @Bean
    fun zalo(): ZaloParty {
        return ZaloParty(props, WebClient.builder().build())
    }

    @Bean
    @ConditionalOnMissingBean
    fun internal(jwtEncoder: JwtEncoder, jwtDecoder: ReactiveJwtDecoder): InternalToken {
        return InternalToken(
            jwtEncoder = jwtEncoder,
            jwtDecoder = jwtDecoder,
            defaultExpireSeconds = props.expireSeconds,
            defaultClaims = props.defaultClaims,
            applicationName = applicationName
        )
    }

}

