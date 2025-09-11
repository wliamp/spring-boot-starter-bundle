package io.github.wliamp.pro.vrf

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(OauthProps::class)
internal class OauthAutoConfig private constructor(
    private val props: OauthProps
) {
    @Bean
    @ConditionalOnProperty(
        prefix = "provider.oauth.facebook",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun fb(): OauthFacebook = OauthFacebook(props.facebook, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.oauth.google",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun gg(): OauthGoogle = OauthGoogle(props.google, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.oauth.zalo",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun zl(): OauthZalo = OauthZalo(props.zalo, WebClient.builder().build())

    @Bean
    @ConditionalOnMissingBean
    fun vrf(
        fb: ObjectProvider<OauthFacebook>,
        gg: ObjectProvider<OauthGoogle>,
        zl: ObjectProvider<OauthZalo>
    ): OauthProvider = OauthProvider(
        fb.ifAvailable,
        gg.ifAvailable,
        zl.ifAvailable
    )
}
