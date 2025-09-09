package io.github.wliamp.pro.vrf

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(Properties::class)
internal class AutoConfig private constructor(
    private val props: Properties
) {
    @Bean("fb")
    @ConditionalOnProperty(
        prefix = "provider.oauth.facebook",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun fb(): IFacebook = IFacebook(props.facebook, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.oauth.google",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun gg(): IGoogle = IGoogle(props.google, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.oauth.zalo",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun zl(): IZalo = IZalo(props.zalo, WebClient.builder().build())

    @Bean
    @ConditionalOnMissingBean
    fun vrf(
        fb: ObjectProvider<IFacebook>,
        gg: ObjectProvider<IGoogle>,
        zl: ObjectProvider<IZalo>
    ): OauthProvider = OauthProvider(
        fb.ifAvailable,
        gg.ifAvailable,
        zl.ifAvailable
    )
}
