package io.github.wliamp.pro.pay

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
    @Bean
    @ConditionalOnProperty(
        prefix = "provider.payment.authorize-net",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun an(): IPay<AuthorizeNetClientData, AuthorizeNetSystemData> = IAuthorizeNet(props.authorizeNet, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.payment.vn-pay",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun vp(): IPay<VnPayClientData, VnPaySystemData> = IVnPay(props.vnPay, WebClient.builder().build())

    @Bean
    @ConditionalOnProperty(
        prefix = "provider.payment.zalo-pay",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun zp(): IPay<ZaloPayClientData, ZaloPaySystemData> = IZaloPay(props.zaloPay, WebClient.builder().build())

    @Bean
    @ConditionalOnMissingBean
    fun pay(
        an: ObjectProvider<IPay<AuthorizeNetClientData, AuthorizeNetSystemData>>,
        vp: ObjectProvider<IPay<VnPayClientData, VnPaySystemData>>,
        zp: ObjectProvider<IPay<ZaloPayClientData, ZaloPaySystemData>>
    ): PaymentProvider = PaymentProvider(
        an.ifAvailable,
        vp.ifAvailable,
        zp.ifAvailable
    )
}
