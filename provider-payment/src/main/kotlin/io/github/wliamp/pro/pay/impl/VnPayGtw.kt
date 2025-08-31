package io.github.wliamp.pro.pay.impl

import io.github.wliamp.pro.pay.config.PaymentProviderProps
import io.github.wliamp.pro.pay.cus.VnPayCus
import io.github.wliamp.pro.pay.sys.VnPaySys
import io.github.wliamp.pro.pay.util.formatDate
import io.github.wliamp.pro.pay.util.generateCode
import io.github.wliamp.pro.pay.util.optional
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.toString

internal class VnPayGtw internal constructor(
    private val props: PaymentProviderProps.VnPayProps,
    private val webClient: WebClient
) : IGtw<VnPayCus, VnPaySys> {
    private val provider = "vnPay"

    @Deprecated(
        message = "Not supported by VNPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun authorize(cus: VnPayCus, sys: VnPaySys): Mono<Any> =
        Mono.error(UnsupportedOperationException("VNPay authorize-only unsupported"))

    @Deprecated(
        message = "Not supported by VNPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun capture(cus: VnPayCus, sys: VnPaySys): Mono<Any> =
        Mono.error(UnsupportedOperationException("VNPay capture unsupported"))

    override fun sale(cus: VnPayCus, sys: VnPaySys): Mono<Any> =
        props.takeIf {
            it.returnUrl.isNotBlank() &&
                it.secretKey.isNotBlank() &&
                it.tmnCode.isNotBlank()
        }?.let { p ->
            val vnpTxnRef = sys.vnpTxnRef ?: generateCode(100)
            val now = LocalDateTime.now()
            val pattern = "yyyyMMddHHmmss"
            val body = mutableMapOf<String, Any>(
                "vnp_Version" to "2.1.0",
                "vnp_Command" to "pay",
                "vnp_TmnCode" to p.tmnCode,
                "vnp_Amount" to (cus.vnpAmount?.toInt()?.times(100)).toString(),
                "vnp_CreateDate" to formatDate(now, pattern),
                "vnp_CurrCode" to "VND",
                "vnp_IpAddr" to (cus.vnpIpAddr ?: "127.0.0.1"),
                "vnp_Locale" to (cus.vnpLocale ?: "vn"),
                "vnp_OrderInfo" to (sys.vnpOrderInfo ?: "Create Order for TxnRef=${vnpTxnRef}"),
                "vnp_OrderType" to (sys.vnpOrderType ?: "other"),
                "vnp_ReturnUrl" to p.returnUrl,
                "vnp_ExpireDate" to formatDate(now.plusMinutes(p.expiredMinutes), pattern),
                "vnp_TxnRef" to vnpTxnRef,
            )
            body.optional("vnp_BankCode", cus.vnpBankCode)
            body.optional("vnp_Bill_Mobile", cus.vnpBillMobile)
            body.optional("vnp_Bill_Email", cus.vnpBillEmail)
            body.optional("vnp_Bill_FirstName", cus.vnpBillFirstName)
            body.optional("vnp_Bill_LastName", cus.vnpBillLastName)
            body.optional("vnp_Bill_Address", cus.vnpBillAddress)
            body.optional("vnp_Bill_City", cus.vnpBillCity)
            body.optional("vnp_Bill_Country", cus.vnpBillCountry)
            body.optional("vnp_Bill_State", cus.vnpBillState)
            body.optional("vnp_Inv_Phone", cus.vnpInvPhone)
            body.optional("vnp_Inv_Email", cus.vnpInvEmail)
            body.optional("vnp_Inv_Customer", cus.vnpInvCustomer)
            body.optional("vnp_Inv_Address", cus.vnpInvAddress)
            body.optional("vnp_Inv_Company", cus.vnpInvCompany)
            body.optional("vnp_Inv_Taxcode", cus.vnpInvTaxcode)
            body.optional("vnp_Inv_Type", cus.vnpInvType)
            val query = querySale(body)
            Mono.just(
                mapOf(
                    "purl" to
                        "${p.baseUrl}${p.saleUri}?$query&vnp_SecureHash=${hmacSHA512(p.secretKey, query)}"
                )
            )
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.payment.vn-pay.return-url' " +
                    "or 'provider.payment.vn-pay.secret-key' " +
                    "or 'provider.payment.vn-pay.tmn-code' " +
                    "for VNPay configurations"
            )
        )

    override fun refund(cus: VnPayCus, sys: VnPaySys): Mono<Any> =
        props.takeIf {
            it.secretKey.isNotBlank() &&
                it.tmnCode.isNotBlank()
        }?.let { p ->
            val pattern = "yyyyMMddHHmmss"
            val requestId = sys.vnpRequestId ?: generateCode(32)
            val version = "2.1.0"
            val command = "refund"
            val tmnCode = p.tmnCode
            val transactionType = sys.vnpTransactionType ?: "02"
            val txnRef = sys.vnpTxnRef ?: generateCode(100)
            val amount = (cus.vnpAmount?.toInt()?.times(100)).toString()
            val transactionNo = sys.vnpTransactionNo ?: ""
            val transactionDate = formatDate(sys.vnpTransactionDate, pattern)
            val createBy = sys.vnpCreateBy ?: "system"
            val createDate = formatDate(LocalDateTime.now(), pattern)
            val ipAddr = sys.vnpIpAddr ?: "127.0.0.1"
            val orderInfo = sys.vnpOrderInfo ?: "Refund order $txnRef"
            val body = mutableMapOf<String, Any>(
                "vnp_RequestId" to requestId,
                "vnp_Version" to version,
                "vnp_Command" to command,
                "vnp_TmnCode" to tmnCode,
                "vnp_TransactionType" to transactionType,
                "vnp_TxnRef" to txnRef,
                "vnp_Amount" to amount,
                "vnp_OrderInfo" to orderInfo,
                "vnp_TransactionNo" to transactionNo,
                "vnp_TransactionDate" to transactionDate,
                "vnp_CreateBy" to createBy,
                "vnp_CreateDate" to createDate,
                "vnp_IpAddr" to ipAddr,
                "vnp_SecureHash" to secureHashRefund(
                    p.secretKey,
                    requestId,
                    version,
                    command,
                    tmnCode,
                    transactionType,
                    txnRef,
                    amount,
                    transactionNo,
                    transactionDate,
                    createBy,
                    createDate,
                    ipAddr,
                    orderInfo
                )
            )
            webClient.post()
                .uri("${p.baseUrl}${p.refundUri}")
                .bodyValue(body)
                .retrieve()
                .onStatus({ status -> status.isError }) { response ->
                    Mono.error(IllegalStateException("VNPay payment failed: ${response.statusCode()}"))
                }
                .bodyToMono(Map::class.java)
                .map { resp -> mapOf("resp" to resp) }
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.payment.vn-pay.secret-key' " +
                    "or 'provider.payment.vn-pay.tmn-code' " +
                    "for VNPay configuration"
            )
        )

    @Deprecated(
        message = "Not supported by VNPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun void(cus: VnPayCus, sys: VnPaySys): Mono<Any> =
        Mono.error(UnsupportedOperationException("VNPay unsupported this action"))

    private fun querySale(body: Map<String, Any>): String =
        body.entries
            .sortedBy { it.key }
            .joinToString("&")
            { "${it.key}=${URLEncoder.encode(it.value.toString(), StandardCharsets.UTF_8.toString())}" }

    private fun secureHashRefund(
        secretKey: String,
        requestId: String,
        version: String,
        command: String,
        tmnCode: String,
        transactionType: String,
        txnRef: String,
        amount: String,
        transactionNo: String,
        transactionDate: String,
        createBy: String,
        createDate: String,
        ipAddr: String,
        orderInfo: String
    ): String =
        hmacSHA512(
            secretKey,
            listOf(
                requestId,
                version,
                command,
                tmnCode,
                transactionType,
                txnRef,
                amount,
                transactionNo,
                transactionDate,
                createBy,
                createDate,
                ipAddr,
                orderInfo
            ).joinToString("|")
        )

    private fun hmacSHA512(key: String, data: String): String =
        Mac.getInstance("HmacSHA512").run {
            init(SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA512"))
            doFinal(data.toByteArray(StandardCharsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
        }
}
