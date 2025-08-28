package io.github.wliamp.pro.vrf.config

import io.github.wliamp.pro.vrf.OauthProvider
import io.github.wliamp.pro.vrf.oauth.FacebookOauth
import io.github.wliamp.pro.vrf.oauth.GoogleOauth
import io.github.wliamp.pro.vrf.oauth.IOauth
import io.github.wliamp.pro.vrf.oauth.ZaloOauth
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(VerifyProviderProps::class)
internal class VerifyProviderAutoConfig private constructor(
    private val facebookProps: VerifyProviderProps.FacebookProps,
    private val googleProps: VerifyProviderProps.GoogleProps,
    private val zaloProps: VerifyProviderProps.ZaloProps
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
