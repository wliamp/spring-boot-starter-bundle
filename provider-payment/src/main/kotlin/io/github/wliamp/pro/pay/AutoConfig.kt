package io.github.wliamp.pro.pay

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(Properties::class)
internal class AutoConfig private constructor(
    private val authorizeNetProps: Properties.AuthorizeNetProps,
    private val vnPayProps: Properties.VnPayProps,
    private val zaloPayProps: Properties.ZaloPayProps
) {
    @Bean
    @ConditionalOnProperty(
        prefix = "provider.payment.authorize-net",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun an(): IPay<AuthorizeNetClientData, AuthorizeNetSystemData> = IAuthorizeNet(authorizeNetProps, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.payment.vn-pay",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun vp(): IPay<VnPayClientData, VnPaySystemData> = IVnPay(vnPayProps, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.payment.zalo-pay",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun zp(): IPay<ZaloPayClientData, ZaloPaySystemData> = IZaloPay(zaloPayProps, WebClient.builder().build())

    @Bean
    @ConditionalOnMissingBean
    fun pay(
        an: IPay<AuthorizeNetClientData, AuthorizeNetSystemData>,
        vp: IPay<VnPayClientData, VnPaySystemData>,
        zp: IPay<ZaloPayClientData, ZaloPaySystemData>
    ): PaymentProvider = PaymentProvider(
        an,
        vp,
        zp
    )
}
