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
@EnableConfigurationProperties(OauthProviderProperties::class)
internal class OauthProviderAutoConfig private constructor(
    private val facebookProps: OauthProviderProperties.FacebookProps,
    private val googleProps: OauthProviderProperties.GoogleProps,
    private val zaloProps: OauthProviderProperties.ZaloProps
) {
    @Bean
    @ConditionalOnProperty(
        prefix = "provider.oauth.facebook",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun fb(): IOauth = FacebookOauth(facebookProps, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.oauth.google",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun gg(): IOauth = GoogleOauth(googleProps, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.oauth.zalo",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun zl(): IOauth = ZaloOauth(zaloProps, WebClient.builder().build())

    @Bean
    @ConditionalOnMissingBean
    fun vrf(
        fb: IOauth,
        gg: IOauth,
        zl: IOauth
    ): OauthProvider = OauthProvider(
        fb,
        gg,
        zl
    )
}
