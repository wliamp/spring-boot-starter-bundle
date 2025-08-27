package io.github.wliamp.pro.pay.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "provider.payment")
data class PaymentProviderProps(
    var vnPay: VnPayProps = VnPayProps()
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
}
