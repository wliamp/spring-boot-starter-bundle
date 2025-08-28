package io.github.wliamp.pro.pay.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "provider.payment")
internal data class PaymentProviderProps(
    var vnPay: VnPayProps = VnPayProps(),
    var authorizeNet: AuthorizeNetProps = AuthorizeNetProps()
) {
    data class VnPayProps(
        var version: String = "2.1.0",
        var baseUrl: String = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html",
        var currency: String = "VND",
        var locale: String = "vn",
        var returnUrl: String = "",
        var secretKey: String = "",
        var tmnCode: String = "",
        var hashSecret: String = ""
    )

    data class AuthorizeNetProps(
        var url: String = "https://apitest.authorize.net/rest/v1/transactions",
        var apiLoginId: String = "",
        var transactionKey: String = "",
    )
}
