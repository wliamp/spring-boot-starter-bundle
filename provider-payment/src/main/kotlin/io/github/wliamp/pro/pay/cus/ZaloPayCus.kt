package io.github.wliamp.pro.pay.cus

data class ZaloPayCus(
    val amount: Long?,
    val description: String?,
    val deviceInfo: String?,
    val embedData: String?,
    val item: String?,
    val bankCode: String?
) : OCus() {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var amount: Long? = null
        private var description: String? = null
        private var deviceInfo: String? = null
        private var embedData: String? = null
        private var item: String? = null
        private var bankCode: String? = null

        fun amount(amount: Long?) = apply { this.amount = amount }
        fun description(description: String?) = apply { this.description = description }
        fun deviceInfo(deviceInfo: String?) = apply { this.deviceInfo = deviceInfo }
        fun embedData(embedData: String?) = apply { this.embedData = embedData }
        fun item(item: String?) = apply { this.item = item }
        fun bankCode(bankCode: String?) = apply { this.bankCode = bankCode }

        fun build() = ZaloPayCus(
            amount, description,
            deviceInfo, embedData,
            item, bankCode
        )
    }
}
