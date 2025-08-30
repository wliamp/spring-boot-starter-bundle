package io.github.wliamp.pro.pay

import io.github.wliamp.pro.pay.gtw.ORequest

data class VnPayRequest(
    val vnpAmount: String,
    val vnpBankCode: String?,
    val vnpCreateBy: String?,
    val vnpIpAddr: String,
    val vnpLocale: String?,
    val vnpOrderInfo: String?,
    val vnpOrderType: String?,
    val vnpTransactionDate: String,
    val vnpTransactionNo: String,
    val vnpTransactionType: String?,
    val vnpTxnRef: String
) : ORequest() {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var vnpAmount: String = ""
        private var vnpBankCode: String? = null
        private var vnpCreateBy: String? = null
        private var vnpIpAddr: String = ""
        private var vnpLocale: String? = null
        private var vnpOrderInfo: String? = null
        private var vnpOrderType: String? = null
        private var vnpTransactionDate: String = ""
        private var vnpTransactionNo: String = ""
        private var vnpTransactionType: String? = null
        private var vnpTxnRef: String = ""

        fun vnpAmount(vnpAmount: String) = apply { this.vnpAmount = vnpAmount }
        fun vnpBankCode(vnpBankCode: String) = apply { this.vnpBankCode = vnpBankCode }
        fun vnpCreateBy(vnpCreateBy: String) = apply { this.vnpCreateBy = vnpCreateBy }
        fun vnpIpAddr(vnpIpAddr: String) = apply { this.vnpIpAddr = vnpIpAddr }
        fun vnpLocale(vnpLocale: String) = apply { this.vnpLocale = vnpLocale }
        fun vnpOrderInfo(vnpOrderInfo: String) = apply { this.vnpOrderInfo = vnpOrderInfo }
        fun vnpOrderType(vnpOrderType: String) = apply { this.vnpOrderType = vnpOrderType }
        fun vnpTransactionDate(vnpTransactionDate: String) = apply { this.vnpTransactionDate = vnpTransactionDate }
        fun vnpTransactionNo(vnpTransactionNo: String) = apply { this.vnpTransactionNo = vnpTransactionNo }
        fun vnpTransactionType(vnpTransactionType: String) = apply { this.vnpTransactionType = vnpTransactionType }
        fun vnpTxnRef(vnpTxnRef: String) = apply { this.vnpTxnRef = vnpTxnRef }

        fun build() = VnPayRequest(
            vnpAmount,
            vnpBankCode,
            vnpCreateBy,
            vnpIpAddr,
            vnpLocale,
            vnpOrderInfo,
            vnpOrderType,
            vnpTransactionDate,
            vnpTransactionNo,
            vnpTransactionType,
            vnpTxnRef
        )
    }
}
