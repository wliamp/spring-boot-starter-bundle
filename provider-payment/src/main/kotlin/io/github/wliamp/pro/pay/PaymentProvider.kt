package io.github.wliamp.pro.pay

class PaymentProvider(
    val authorizeNet: IPayment<AuthorizeNetClientData, AuthorizeNetSystemData>?,
    val vnPay: IPayment<VnPayClientData, VnPaySystemData>?,
    val zaloPay: IPayment<ZaloPayClientData, ZaloPaySystemData>?
) {
    fun of(payment: Payment): IPayment<*, *>? =
        when (payment) {
            Payment.AUTHORIZE_NET -> authorizeNet
            Payment.VN_PAY -> vnPay
            Payment.ZALO_PAY -> zaloPay
        }
}
