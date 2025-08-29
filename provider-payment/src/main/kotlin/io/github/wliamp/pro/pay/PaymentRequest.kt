package io.github.wliamp.pro.pay

data class PaymentRequest(
    val amount: String,
    val orderId: String,
    val orderType: String?,
    val currency: String,
    val locale: String,
    val description: String?,
    val ipAddress: String,
    val transactionType: String?,
    val transactionNo: String,
    val transactionDate: String,
    val createBy: String?,
    val refTransId: String
)
