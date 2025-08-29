package io.github.wliamp.pro.pay

import io.github.wliamp.pro.pay.gtw.IGtw
import org.springframework.stereotype.Component

@Component
class PaymentProvider(
    val authorizeNet: IGtw,
    val vnPay: IGtw,
    val zaloPay: IGtw
)
