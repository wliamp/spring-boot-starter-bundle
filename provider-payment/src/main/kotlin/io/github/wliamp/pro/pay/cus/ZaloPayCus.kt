package io.github.wliamp.pro.pay.cus

data class ZaloPayCus(
    val amount: String,
    val appUser: String?,
    val appTransId: String,
    val callbackUrl: String?,
    val description: String?,
    val embedData: String?,
    val item: String?,
    val orderCode: String?,
    val paymentId: String?,
    val preferredPaymentMethod: String?,
    val zpTransId: String
) : OCus() {
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

        fun build() = ZaloPayCus(
            amount,
            appTransId,
            description,
            zpTransId
        )
    }
}
