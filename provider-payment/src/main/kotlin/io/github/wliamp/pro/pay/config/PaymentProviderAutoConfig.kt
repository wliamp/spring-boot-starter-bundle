package io.github.wliamp.pro.pay.config

import io.github.wliamp.pro.pay.PaymentProvider
import io.github.wliamp.pro.pay.impl.AuthorizeNetGtw
import io.github.wliamp.pro.pay.impl.IGtw
import io.github.wliamp.pro.pay.impl.VnPayGtw
import io.github.wliamp.pro.pay.impl.ZaloPayGtw
import io.github.wliamp.pro.pay.cus.AuthorizeNetCus
import io.github.wliamp.pro.pay.cus.VnPayCus
import io.github.wliamp.pro.pay.cus.ZaloPayCus
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
    private val vnPayProps: PaymentProviderProps.VnPayProps,
    private val zaloPayProps: PaymentProviderProps.ZaloPayProps
) {
    @Bean
    @ConditionalOnProperty(
        prefix = "provider.payment.authorize-net",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun an(): IGtw<AuthorizeNetCus> = AuthorizeNetGtw(authorizeNetProps, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.payment.vn-pay",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun vp(): IGtw<VnPayCus> = VnPayGtw(vnPayProps, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.payment.zalo-pay",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun zp(): IGtw<ZaloPayCus> = ZaloPayGtw(zaloPayProps, WebClient.builder().build())

    @Bean
    @ConditionalOnMissingBean
    fun pay(
        an: IGtw<AuthorizeNetCus>,
        vp: IGtw<VnPayCus>,
        zp: IGtw<ZaloPayCus>
    ): PaymentProvider = PaymentProvider(
        an,
        vp,
        zp)
}
