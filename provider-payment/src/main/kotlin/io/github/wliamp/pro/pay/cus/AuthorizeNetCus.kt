package io.github.wliamp.pro.pay.cus

data class AuthorizeNetCus(
    val amount: String,
    val description: String?,
    val orderId: String,
    val refTransId: String,
) : OCus() {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var amount: String = ""
        private var description: String? = null
        private var orderId: String = ""
        private var refTransId: String = ""

        fun amount(amount: String) = apply { this.amount = amount }
        fun description(description: String?) = apply { this.description = description }
        fun orderId(orderId: String) = apply { this.orderId = orderId }
        fun refTransId(refTransId: String) = apply { this.refTransId = refTransId }

        fun build() = AuthorizeNetCus(
            amount,
            description,
            orderId,
            refTransId
        )
    }
}
