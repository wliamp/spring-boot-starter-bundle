package io.github.wliamp.pro.pay.sys

import io.github.wliamp.pro.pay.cus.ZaloPayCus

data class ZaloPaySys(
    val appUser: String?,
    val appTransId: String,
    val callbackUrl: String?,
    val zpTransToken: String,
) : OSys() {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var appUser: String? = null
        private var appTransId: String = ""
        private var callbackUrl: String? = null
        private var zpTransToken: String = ""
        private var item: String = ""
        private var bankCode: String? = null

        fun appUser(appUser: String?) = apply { this.appUser = appUser }
        fun appTransId(appTransId: String) = apply { this.appTransId = appTransId }
        fun callbackUrl(callbackUrl: String?) = apply { this.callbackUrl = callbackUrl }
        fun zpTransToken(zpTransToken: String) = apply { this.zpTransToken = zpTransToken }

        fun build() = ZaloPaySys(
            appUser,
            appTransId,
            callbackUrl,
            zpTransToken
        )
    }
}
