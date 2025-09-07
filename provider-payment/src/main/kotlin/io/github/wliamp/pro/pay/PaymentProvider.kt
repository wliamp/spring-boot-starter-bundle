package io.github.wliamp.pro.pay

import io.github.wliamp.pro.pay.cus.AuthorizeNetCus
import io.github.wliamp.pro.pay.cus.VnPayCus
import io.github.wliamp.pro.pay.cus.ZaloPayCus
import io.github.wliamp.pro.pay.impl.IGtw
import io.github.wliamp.pro.pay.sys.AuthorizeNetSys
import io.github.wliamp.pro.pay.sys.VnPaySys
import io.github.wliamp.pro.pay.sys.ZaloPaySys
import org.springframework.stereotype.Component

@Component
class PaymentProvider(
    val authorizeNet: IGtw<AuthorizeNetCus, AuthorizeNetSys>,
    val vnPay: IGtw<VnPayCus, VnPaySys>,
    val zaloPay: IGtw<ZaloPayCus, ZaloPaySys>
)
