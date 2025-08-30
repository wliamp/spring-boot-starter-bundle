package io.github.wliamp.pro.pay.impl

import io.github.wliamp.pro.pay.config.PaymentProviderProps
import io.github.wliamp.pro.pay.req.VnPayRequest
import io.github.wliamp.pro.pay.util.putIfNotBlank
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.*

internal class VnPayPayment internal constructor(
    private val props: PaymentProviderProps.VnPayProps, private val webClient: WebClient
) : IPayment<VnPayRequest> {
    private val provider = "vnPay"

    @Deprecated(
        message = "Not supported by VNPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun authorize(request: VnPayRequest): Mono<Any> =
        Mono.error(UnsupportedOperationException("VNPay authorize-only unsupported"))

    @Deprecated(
        message = "Not supported by VNPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun capture(request: VnPayRequest): Mono<Any> =
        Mono.error(UnsupportedOperationException("VNPay capture unsupported"))

    override fun sale(request: VnPayRequest): Mono<Any> =
        props.takeIf {
            it.hashSecret.isNotBlank() &&
                it.returnUrl.isNotBlank() &&
                it.tmnCode.isNotBlank()
        }?.let { p ->
            val now = LocalDateTime.now()
            val body = mutableMapOf(
                "vnp_Version" to p.version,
                "vnp_Command" to "pay",
                "vnp_CurrCode" to "VND",
                "vnp_TmnCode" to p.tmnCode,
                "vnp_ReturnUrl" to p.returnUrl,
                "vnp_IpAddr" to request.vnpIpAddr,
                "vnp_TxnRef" to request.vnpTxnRef,
                "vnp_Amount" to (request.vnpAmount.toInt() * 100).toString(),
                "vnp_CreateDate" to formatDate(now),
                "vnp_ExpireDate" to formatDate(now.plusMinutes(p.expiredMinutes))
            )
            body.putIfNotBlank("vnp_BankCode", request.vnpBankCode)
            body.putIfNotBlank("vnp_Locale", request.vnpLocale)
            body.putIfNotBlank("vnp_OrderInfo", request.vnpOrderInfo)
            body.putIfNotBlank("vnp_OrderType", request.vnpOrderType)
            val query = body.entries
                .sortedBy { it.key }
                .joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, StandardCharsets.UTF_8)}" }
            Mono.just("${p.baseUrl}?$query&vnp_SecureHash=${hmacSHA512(p.hashSecret, query)}")
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.payment.vn-pay.hash-secret' " +
                    "or 'provider.payment.vn-pay.tmn-code' " +
                    "or 'provider.payment.vn-pay.return-url' " +
                    "for VNPay configuration"
            )
        )

    override fun refund(request: VnPayRequest): Mono<Any> =
        props.takeIf {
            it.hashSecret.isNotBlank() &&
                it.tmnCode.isNotBlank()
        }?.let { p ->
            val body = mutableMapOf(
                "vnp_Version" to p.version,
                "vnp_Command" to "refund",
                "vnp_RequestId" to UUID.randomUUID().toString(),
                "vnp_TmnCode" to p.tmnCode,
                "vnp_IpAddr" to request.vnpIpAddr,
                "vnp_TxnRef" to request.vnpTxnRef,
                "vnp_OrderInfo" to (request.vnpOrderInfo ?: ("Refund order " + request.vnpTxnRef)),
                "vnp_Amount" to (request.vnpAmount.toInt() * 100).toString(),
                "vnp_TransactionType" to (request.vnpTransactionType ?: "02"),
                "vnp_TransactionDate" to formatDate(request.vnpTransactionDate),
                "vnp_CreateBy" to (request.vnpCreateBy ?: "system")
            )
            body.putIfNotBlank("vnp_Locale", request.vnpLocale)
            body.putIfNotBlank("vnp_TransactionNo", request.vnpTransactionNo)
            val query = body + ("vnp_SecureHash" to hmacSHA512(
                p.hashSecret,
                body.entries
                    .sortedBy { it.key }
                    .joinToString("&") { "${it.key}=${it.value}" }))
            webClient.post()
                .uri("${p.baseUrl}/merchant_webapi/api/transaction")
                .bodyValue(query)
                .retrieve()
                .onStatus({ status -> status.isError }) { response ->
                Mono.error(IllegalStateException("VNPay payment failed: ${response.statusCode()}"))
            }.bodyToMono(Map::class.java).map { resp ->
                mapOf(
                    "success" to (resp["vnp_ResponseCode"] == "00"),
                    "resp" to resp
                )
            }
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.payment.vn-pay.hash-secret' " +
                    "or 'provider.payment.vn-pay.tmn-code' " +
                    "for VNPay configuration"
            )
        )

    @Deprecated(
        message = "Not supported by VNPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun void(request: VnPayRequest): Mono<Any> =
        Mono.error(UnsupportedOperationException("VNPay unsupported this action"))

    private fun hmacSHA512(key: String, data: String): String =
        Mac.getInstance("HmacSHA512").run {
        init(SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA512"))
        doFinal(data.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private fun formatDate(input: Any): String = when (input) {
        is String -> runCatching {
            LocalDateTime.parse(input, DateTimeFormatter.ISO_DATE_TIME)
        }.getOrElse {
            LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        }

        is Long -> LocalDateTime.ofInstant(Instant.ofEpochMilli(input), ZoneId.systemDefault())
        is LocalDateTime -> input
        else -> throw IllegalArgumentException("Unsupported date type for VNPay: ${input::class}")
    }.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
}
