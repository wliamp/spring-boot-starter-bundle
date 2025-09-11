package io.github.wliamp.pro.pay

class PaymentProvider(
    val authorizeNet: IPay<AuthorizeNetClientData, AuthorizeNetSystemData>?,
    val vnPay: IPay<VnPayClientData, VnPaySystemData>?,
    val zaloPay: IPay<ZaloPayClientData, ZaloPaySystemData>?
) {
    fun of(payment: Payment): IPay<*, *>? =
        when (payment) {
            Payment.AUTHORIZE_NET -> authorizeNet
            Payment.VN_PAY -> vnPay
            Payment.ZALO_PAY -> zaloPay
        }
}
