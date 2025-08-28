package io.github.wliamp.pro.vrf.config

import io.github.wliamp.pro.vrf.data.FacebookOauth
import io.github.wliamp.pro.vrf.data.GoogleOauth
import io.github.wliamp.pro.vrf.data.IOauth
import io.github.wliamp.pro.vrf.data.ZaloOauth
import io.github.wliamp.pro.vrf.util.OauthProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(VerifyProviderProperties::class)
class VerifyProviderAutoConfig(
    private val props: VerifyProviderProperties,
) {
    @Bean
    @ConditionalOnProperty(prefix = "oauth.facebook", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun fb(): IOauth = FacebookOauth(props, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(prefix = "oauth.google", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun gg(): IOauth = GoogleOauth(props, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(prefix = "oauth.zalo", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun zl(): IOauth = ZaloOauth(props, WebClient.builder().build())

    @Bean
    @ConditionalOnMissingBean
    fun vrf(
        fb: IOauth,
        gg: IOauth,
        zl: IOauth
    ): OauthProvider = OauthProvider(fb, gg, zl)
}
