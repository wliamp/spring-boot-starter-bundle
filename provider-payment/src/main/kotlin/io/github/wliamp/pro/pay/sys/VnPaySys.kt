package io.github.wliamp.pro.pay.sys

data class VnPaySys(
    val vnpCreateBy: String?,
    val vnpIpAddr: String?,
    val vnpOrderInfo: String?,
    val vnpOrderType: String?,
    val vnpTransactionDate: String?,
    val vnpTransactionNo: String?,
    val vnpTransactionType: String?,
    val vnpTxnRef: String?,
    val vnpRequestId: String?

): OSys(){
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var vnpOrderInfo: String? = null
        private var vnpOrderType: String? = null
        private var vnpTransactionDate: String = ""
        private var vnpTransactionNo: String = ""
        private var vnpTransactionType: String? = null
        private var vnpTxnRef: String? = null

        fun vnpOrderInfo(vnpOrderInfo: String) = apply { this.vnpOrderInfo = vnpOrderInfo }
        fun vnpOrderType(vnpOrderType: String) = apply { this.vnpOrderType = vnpOrderType }
        fun vnpTransactionDate(vnpTransactionDate: String) = apply { this.vnpTransactionDate = vnpTransactionDate }
        fun vnpTransactionNo(vnpTransactionNo: String) = apply { this.vnpTransactionNo = vnpTransactionNo }
        fun vnpTransactionType(vnpTransactionType: String) = apply { this.vnpTransactionType = vnpTransactionType }
        fun vnpTxnRef(vnpTxnRef: String?) = apply { this.vnpTxnRef = vnpTxnRef }

        fun build() = VnPaySys(
            vnpOrderInfo,
            vnpOrderType,
            vnpTransactionDate,
            vnpTransactionNo,
            vnpTransactionType,
            vnpTxnRef
        )
    }
}
