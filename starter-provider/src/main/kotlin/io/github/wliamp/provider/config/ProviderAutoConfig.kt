package io.github.wliamp.provider.config

import io.github.wliamp.provider.data.FacebookOauth
import io.github.wliamp.provider.data.GoogleOauth
import io.github.wliamp.provider.data.Oauth
import io.github.wliamp.provider.data.ZaloOauth
import io.github.wliamp.provider.util.OauthVerifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(ProviderProperties::class)
class ProviderAutoConfig(
    private val props: ProviderProperties,
) {
    @Bean
    @ConditionalOnProperty(prefix = "oauth.facebook", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun fb(): Oauth = FacebookOauth(props, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(prefix = "oauth.google", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun gg(): Oauth = GoogleOauth(props, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(prefix = "oauth.zalo", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun zl(): Oauth = ZaloOauth(props, WebClient.builder().build())

    @Bean
    @ConditionalOnMissingBean
    fun external(
        fb: Oauth,
        gg: Oauth,
        zl: Oauth
    ): OauthVerifier = OauthVerifier(fb, gg, zl)
}
