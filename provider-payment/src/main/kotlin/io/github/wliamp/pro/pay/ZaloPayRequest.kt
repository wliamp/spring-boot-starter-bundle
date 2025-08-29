package io.github.wliamp.pro.pay

import io.github.wliamp.pro.pay.gtw.ORequest

data class ZaloPayRequest(
    val appTransId: String,
    val amount: String,
    val description: String?,
    val zpTransId: String
): ORequest()
