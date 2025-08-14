package io.wliamp.token.config

import io.wliamp.token.util.verifier.ThirdPartyToken
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(TokenProperties::class)
class TokenAutoConfig {
    @Bean
    fun tokenVerifier(props: TokenProperties): ThirdPartyToken {
        return ThirdPartyToken(props, WebClient.builder().build())
    }
}
