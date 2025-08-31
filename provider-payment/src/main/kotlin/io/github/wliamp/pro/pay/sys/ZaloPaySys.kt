package io.github.wliamp.pro.pay.sys

data class ZaloPaySys(
    val appUser: String?,
    val appTransId: String,
    val callbackUrl: String?,
    val refundFeeAmount: Long?,
    val zpTransId: String,
) : OSys() {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var appUser: String? = null
        private var appTransId: String = ""
        private var callbackUrl: String? = null
        private var zpTransId: String = ""
        private var refundFeeAmount: Long? = null
        private var bankCode: String? = null

        fun appUser(appUser: String?) = apply { this.appUser = appUser }
        fun appTransId(appTransId: String) = apply { this.appTransId = appTransId }
        fun callbackUrl(callbackUrl: String?) = apply { this.callbackUrl = callbackUrl }
        fun refundFeeAmount(refundFeeAmount: Long?) = apply { this.refundFeeAmount = refundFeeAmount }
        fun zpTransId(zpTransId: String) = apply { this.zpTransId = zpTransId }

        fun build() = ZaloPaySys(
            appUser,
            appTransId,
            callbackUrl,
            refundFeeAmount,
            zpTransId
        )
    }
}
