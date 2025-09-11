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
internal class OtpAutoConfig private constructor(
    private val props: OtpProps
) {
    @Bean
    @ConditionalOnProperty(
        prefix = "provider.otp.firebase",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun f(): OtpFirebase = OtpFirebase(props.firebase, WebClient.builder().build())

    @Bean
    @ConditionalOnMissingBean
    fun otp(
        f: ObjectProvider<OtpFirebase>
    ): OtpProvider = OtpProvider(
        f.ifAvailable
    )
}
