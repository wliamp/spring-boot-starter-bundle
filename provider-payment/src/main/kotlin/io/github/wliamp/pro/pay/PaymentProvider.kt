package io.github.wliamp.pro.pay

import org.springframework.stereotype.Component

@Component
class PaymentProvider(
    val authorizeNet: IPay<AuthorizeNetClientData, AuthorizeNetSystemData>,
    val vnPay: IPay<VnPayClientData, VnPaySystemData>,
    val zaloPay: IPay<ZaloPayClientData, ZaloPaySystemData>
)
