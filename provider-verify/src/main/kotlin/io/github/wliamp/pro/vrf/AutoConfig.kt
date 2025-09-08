package io.github.wliamp.pro.vrf

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(Properties::class)
internal class AutoConfig private constructor(
    private val facebookProps: Properties.FacebookProps,
    private val googleProps: Properties.GoogleProps,
    private val zaloProps: Properties.ZaloProps
) {
    @Bean
    @ConditionalOnProperty(
        prefix = "provider.oauth.facebook",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun fb(): IOauth = IFacebook(facebookProps, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.oauth.google",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun gg(): IOauth = IGoogle(googleProps, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.oauth.zalo",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun zl(): IOauth = IZalo(zaloProps, WebClient.builder().build())

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
