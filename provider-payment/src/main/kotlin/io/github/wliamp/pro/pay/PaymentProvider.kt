package io.github.wliamp.pro.pay

import io.github.wliamp.pro.pay.gtw.IGtw
import org.springframework.stereotype.Component

@Component
class PaymentProvider(
    val authorizeNet: IGtw<AuthorizeNetRequest>,
    val vnPay: IGtw<VnPayRequest>,
    val zaloPay: IGtw<ZaloPayRequest>
)
