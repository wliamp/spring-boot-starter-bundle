package io.github.wliamp.pro.pay.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "provider.payment")
internal data class PaymentProviderProps(
    var authorizeNet: AuthorizeNetProps = AuthorizeNetProps(),
    var vnPay: VnPayProps = VnPayProps(),
    var zaloPay: ZaloPayProps = ZaloPayProps()
) {
    data class AuthorizeNetProps(
        var baseUrl: String = "https://api2.authorize.net/xml/v1/request.api",
        var redirectUrl: String = "https://accept.authorize.net/payment/payment",
        var apiLoginId: String = "",
        var transactionKey: String = "",
        var returnUrl: String = "",
        var cancelUrl: String = "",
    )

    data class VnPayProps(
        var baseUrl: String = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html",
        var version: String = "2.1.0",
        var expiredMinutes: Long = 15,
        var hashSecret: String = "",
        var returnUrl: String = "",
        var tmnCode: String = ""
    )

    data class ZaloPayProps(
        val baseUrl: String = "https://sb-openapi.zalopay.vn",
        val saleUri: String = "/v2/create",
        val refundUri: String = "/v2/refund",
        val expireDurationSeconds: Long = 86400,
        val appId: Int = 0,
        val macKey: String = "",
        val subAppId: String? = ""
    )
}
