package io.github.wliamp.pro.pay

import io.github.wliamp.pro.pay.gtw.ORequest

data class VnPayRequest(
    val vnpAmount: String,
    val vnpTxnRef: String,
    val vnpOrderInfo: String?,
    val vnpOrderType: String?,
    val vnpIpAddr: String,
    val vnpTransactionType: String?,
    val vnpTransactionNo: String,
    val vnpTransactionDate: String,
    val vnpCreateBy: String?
): ORequest()
