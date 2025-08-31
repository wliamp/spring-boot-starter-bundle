package io.github.wliamp.pro.pay.cus

data class AuthorizeNetCus(
    val amount: String?,
) : OCus() {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var amount: String? = null

        fun amount(amount: String?) = apply { this.amount = amount }

        fun build() = AuthorizeNetCus(
            amount
        )
    }
}
