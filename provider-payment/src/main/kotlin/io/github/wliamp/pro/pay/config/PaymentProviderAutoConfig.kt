package io.github.wliamp.pro.pay.config

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
class PaymentProviderAutoConfig(
    private val props: PaymentProviderProps,
) {
    @Bean
    @ConditionalOnProperty(prefix = "payment.vnpay", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun vp(): IGtw = VnPayGtw(props, WebClient.builder().build())

    @Bean
    @ConditionalOnMissingBean
    fun external(
        vp: IGtw
    ): PaymentProvider = PaymentProvider(vp)
}
