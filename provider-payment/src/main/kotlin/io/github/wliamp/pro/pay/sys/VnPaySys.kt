package io.github.wliamp.pro.pay.sys

data class VnPaySys(
    val vnpCreateBy: String?,
    val vnpIpAddr: String?,
    val vnpOrderInfo: String?,
    val vnpOrderType: String?,
    val vnpRequestId: String?,
    val vnpTransactionDate: String?,
    val vnpTransactionNo: String?,
    val vnpTransactionType: String?,
    val vnpTxnRef: String?

) : OSys() {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var vnpCreateBy: String? = null
        private var vnpIpAddr: String? = null
        private var vnpOrderInfo: String? = null
        private var vnpOrderType: String? = null
        private var vnpRequestId: String? = null
        private var vnpTransactionDate: String? = null
        private var vnpTransactionNo: String? = null
        private var vnpTransactionType: String? = null
        private var vnpTxnRef: String? = null

        fun vnpCreateBy(vnpCreateBy: String?) = apply { this.vnpCreateBy = vnpCreateBy }
        fun vnpIpAddr(vnpIpAddr: String?) = apply { this.vnpIpAddr = vnpIpAddr }
        fun vnpOrderInfo(vnpOrderInfo: String?) = apply { this.vnpOrderInfo = vnpOrderInfo }
        fun vnpOrderType(vnpOrderType: String?) = apply { this.vnpOrderType = vnpOrderType }
        fun vnpRequestId(vnpRequestId: String?) = apply { this.vnpRequestId = vnpRequestId }
        fun vnpTransactionDate(vnpTransactionDate: String?) = apply { this.vnpTransactionDate = vnpTransactionDate }
        fun vnpTransactionNo(vnpTransactionNo: String?) = apply { this.vnpTransactionNo = vnpTransactionNo }
        fun vnpTransactionType(vnpTransactionType: String?) = apply { this.vnpTransactionType = vnpTransactionType }
        fun vnpTxnRef(vnpTxnRef: String?) = apply { this.vnpTxnRef = vnpTxnRef }

        fun build() = VnPaySys(
            vnpCreateBy, vnpIpAddr,
            vnpOrderInfo, vnpOrderType,
            vnpRequestId, vnpTransactionDate,
            vnpTransactionNo, vnpTransactionType,
            vnpTxnRef
        )
    }
}
