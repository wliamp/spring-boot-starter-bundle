package io.github.wliamp.pro.pay

data class VnPayClientData(
    val vnpAmount: String?,
    val vnpBankCode: String?,
    val vnpCreateBy: String?,
    val vnpIpAddr: String?,
    val vnpLocale: String?,
    val vnpBillMobile: String?,
    val vnpBillEmail: String?,
    val vnpBillFirstName: String?,
    val vnpBillLastName: String?,
    val vnpBillAddress: String?,
    val vnpBillCity: String?,
    val vnpBillCountry: String?,
    val vnpBillState: String?,
    val vnpInvPhone: String?,
    val vnpInvEmail: String?,
    val vnpInvCustomer: String?,
    val vnpInvAddress: String?,
    val vnpInvCompany: String?,
    val vnpInvTaxcode: String?,
    val vnpInvType: String?
) : OClient() {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var vnpAmount: String? = null
        private var vnpBankCode: String? = null
        private var vnpCreateBy: String? = null
        private var vnpIpAddr: String? = null
        private var vnpLocale: String? = null
        private var vnpTransactionDate: String? = null
        private var vnpTransactionNo: String? = null
        private var vnpTransactionType: String? = null
        private var vnpBillMobile: String? = null
        private var vnpBillEmail: String? = null
        private var vnpBillFirstName: String? = null
        private var vnpBillLastName: String? = null
        private var vnpBillAddress: String? = null
        private var vnpBillCity: String? = null
        private var vnpBillCountry: String? = null
        private var vnpBillState: String? = null
        private var vnpInvPhone: String? = null
        private var vnpInvEmail: String? = null
        private var vnpInvCustomer: String? = null
        private var vnpInvAddress: String? = null
        private var vnpInvCompany: String? = null
        private var vnpInvTaxcode: String? = null
        private var vnpInvType: String? = null

        fun vnpAmount(vnpAmount: String?) = apply { this.vnpAmount = vnpAmount }
        fun vnpBankCode(vnpBankCode: String?) = apply { this.vnpBankCode = vnpBankCode }
        fun vnpCreateBy(vnpCreateBy: String?) = apply { this.vnpCreateBy = vnpCreateBy }
        fun vnpIpAddr(vnpIpAddr: String?) = apply { this.vnpIpAddr = vnpIpAddr }
        fun vnpLocale(vnpLocale: String?) = apply { this.vnpLocale = vnpLocale }
        fun vnpTransactionDate(vnpTransactionDate: String?) = apply { this.vnpTransactionDate = vnpTransactionDate }
        fun vnpTransactionNo(vnpTransactionNo: String?) = apply { this.vnpTransactionNo = vnpTransactionNo }
        fun vnpTransactionType(vnpTransactionType: String?) = apply { this.vnpTransactionType = vnpTransactionType }
        fun vnpBillMobile(vnpBillMobile: String?) = apply { this.vnpBillMobile = vnpBillMobile }
        fun vnpBillEmail(vnpBillEmail: String?) = apply { this.vnpBillEmail = vnpBillEmail }
        fun vnpBillFirstName(vnpBillFirstName: String?) = apply { this.vnpBillFirstName = vnpBillFirstName }
        fun vnpBillLastName(vnpBillLastName: String?) = apply { this.vnpBillLastName = vnpBillLastName }
        fun vnpBillAddress(vnpBillAddress: String?) = apply { this.vnpBillAddress = vnpBillAddress }
        fun vnpBillCity(vnpBillCity: String?) = apply { this.vnpBillCity = vnpBillCity }
        fun vnpBillCountry(vnpBillCountry: String?) = apply { this.vnpBillCountry = vnpBillCountry }
        fun vnpBillState(vnpBillState: String?) = apply { this.vnpBillState = vnpBillState }
        fun vnpInvPhone(vnpInvPhone: String?) = apply { this.vnpInvPhone = vnpInvPhone }
        fun vnpInvEmail(vnpInvEmail: String?) = apply { this.vnpInvEmail = vnpInvEmail }
        fun vnpInvCustomer(vnpInvCustomer: String?) = apply { this.vnpInvCustomer = vnpInvCustomer }
        fun vnpInvAddress(vnpInvAddress: String?) = apply { this.vnpInvAddress = vnpInvAddress }
        fun vnpInvCompany(vnpInvCompany: String?) = apply { this.vnpInvCompany = vnpInvCompany }
        fun vnpInvTaxcode(vnpInvTaxcode: String?) = apply { this.vnpInvTaxcode = vnpInvTaxcode }
        fun vnpInvType(vnpInvType: String?) = apply { this.vnpInvType = vnpInvType }

        fun build() = VnPayClientData(
            vnpAmount, vnpBankCode, vnpCreateBy, vnpIpAddr,
            vnpLocale, vnpBillMobile, vnpBillEmail, vnpBillFirstName,
            vnpBillLastName, vnpBillAddress, vnpBillCity, vnpBillCountry,
            vnpBillState, vnpInvPhone, vnpInvEmail, vnpInvCustomer,
            vnpInvAddress, vnpInvCompany, vnpInvTaxcode, vnpInvType
        )
    }
}

data class VnPaySystemData(
    val vnpCreateBy: String?,
    val vnpIpAddr: String?,
    val vnpOrderInfo: String?,
    val vnpOrderType: String?,
    val vnpRequestId: String?,
    val vnpTransactionDate: String?,
    val vnpTransactionNo: String?,
    val vnpTransactionType: String?,
    val vnpTxnRef: String?

) : OSystem() {
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

        fun build() = VnPaySystemData(
            vnpCreateBy, vnpIpAddr,
            vnpOrderInfo, vnpOrderType,
            vnpRequestId, vnpTransactionDate,
            vnpTransactionNo, vnpTransactionType,
            vnpTxnRef
        )
    }
}
