package io.github.wliamp.pro.pay.config

import io.github.wliamp.pro.pay.PaymentProvider
import io.github.wliamp.pro.pay.impl.AuthorizeNetPayment
import io.github.wliamp.pro.pay.impl.IPayment
import io.github.wliamp.pro.pay.impl.VnPayPayment
import io.github.wliamp.pro.pay.impl.ZaloPayPayment
import io.github.wliamp.pro.pay.req.AuthorizeNetRequest
import io.github.wliamp.pro.pay.req.VnPayRequest
import io.github.wliamp.pro.pay.req.ZaloPayRequest
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
    fun an(): IPayment<AuthorizeNetRequest> = AuthorizeNetPayment(authorizeNetProps, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.payment.vn-pay",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun vp(): IPayment<VnPayRequest> = VnPayPayment(vnPayProps, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.payment.zalo-pay",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun zp(): IPayment<ZaloPayRequest> = ZaloPayPayment(zaloPayProps, WebClient.builder().build())

    @Bean
    @ConditionalOnMissingBean
    fun pay(
        an: IPayment<AuthorizeNetRequest>,
        vp: IPayment<VnPayRequest>,
        zp: IPayment<ZaloPayRequest>
    ): PaymentProvider = PaymentProvider(
        an,
        vp,
        zp)
}
