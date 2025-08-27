package io.github.wliamp.pro.pay.util

import io.github.wliamp.pro.pay.data.IGtw
import org.springframework.stereotype.Component

@Component
class PaymentProvider(
    val vnpay: IGtw
)


