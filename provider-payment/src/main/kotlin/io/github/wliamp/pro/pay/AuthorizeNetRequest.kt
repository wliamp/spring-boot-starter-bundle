package io.github.wliamp.pro.pay

import io.github.wliamp.pro.pay.gtw.ORequest

data class AuthorizeNetRequest(
    val amount: String,
    val refTransId: String,
    val orderId: String,
    val description: String?
): ORequest()
