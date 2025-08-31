package io.github.wliamp.pro.pay.sys

data class AuthorizeNetSys(
    val description: String?,
    val orderId: String?,
    val refTransId: String?,
) : OSys() {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var description: String? = null
        private var orderId: String? = null
        private var refTransId: String? = null

        fun description(description: String?) = apply { this.description = description }
        fun orderId(orderId: String?) = apply { this.orderId = orderId }
        fun refTransId(refTransId: String?) = apply { this.refTransId = refTransId }

        fun build() = AuthorizeNetSys(
            description,
            orderId,
            refTransId
        )
    }
}
