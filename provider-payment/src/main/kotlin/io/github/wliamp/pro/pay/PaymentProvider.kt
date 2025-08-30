package io.github.wliamp.pro.pay

import io.github.wliamp.pro.pay.impl.IPayment
import org.springframework.stereotype.Component

@Component
class PaymentProvider(
    val authorizeNet: IPayment<AuthorizeNetRequest>,
    val vnPay: IPayment<VnPayRequest>,
    val zaloPay: IPayment<ZaloPayRequest>
)
