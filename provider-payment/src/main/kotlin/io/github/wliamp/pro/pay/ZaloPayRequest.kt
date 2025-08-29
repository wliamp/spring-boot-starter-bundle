package io.github.wliamp.pro.pay

import io.github.wliamp.pro.pay.gtw.ORequest

data class ZaloPayRequest(
    val amount: String,
    val appTransId: String,
    val description: String?,
    val zpTransId: String
) : ORequest() {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var amount: String = ""
        private var appTransId: String = ""
        private var description: String? = null
        private var zpTransId: String = ""

        fun amount(amount: String) = apply { this.amount = amount }
        fun appTransId(appTransId: String) = apply { this.appTransId = appTransId }
        fun description(description: String?) = apply { this.description = description }
        fun zpTransId(zpTransId: String) = apply { this.zpTransId = zpTransId }

        fun build() = ZaloPayRequest(
            amount,
            appTransId,
            description,
            zpTransId
        )
    }
}
