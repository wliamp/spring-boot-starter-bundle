package io.github.wliamp.pro.pay.config

import io.github.wliamp.pro.pay.data.AuthorizeNetGtw
import io.github.wliamp.pro.pay.data.IGtw
import io.github.wliamp.pro.pay.data.VnPayGtw
import io.github.wliamp.pro.pay.util.PaymentProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(PaymentProviderProps::class)
internal class PaymentProviderAutoConfig private constructor(
    private val authorizeNetProps: PaymentProviderProps.AuthorizeNetProps,
    private val vnPayProps: PaymentProviderProps.VnPayProps
) {
    @Bean
    @ConditionalOnProperty(
        prefix = "provider.payment.authorize-net",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun an(): IGtw = AuthorizeNetGtw(authorizeNetProps, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.payment.vn-pay",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun vp(): IGtw = VnPayGtw(vnPayProps, WebClient.builder().build())

    @Bean
    @ConditionalOnMissingBean
    fun pay(
        an: IGtw,
        vp: IGtw
    ): PaymentProvider = PaymentProvider(an, vp)
}
