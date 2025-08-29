package io.github.wliamp.pro.pay.gtw

import io.github.wliamp.pro.pay.PaymentRequest
import io.github.wliamp.pro.pay.config.PaymentProviderProps
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.*

internal class VnPayGtw internal constructor(
    private val props: PaymentProviderProps.VnPayProps,
    private val webClient: WebClient
) : IGtw {
    private val provider = "vnPay"

    @Deprecated(
        message = "Not supported by VNPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun authorize(body: PaymentRequest): Mono<Any> =
        Mono.error(UnsupportedOperationException("VNPay authorize-only unsupported"))

    @Deprecated(
        message = "Not supported by VNPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun capture(body: PaymentRequest): Mono<Any> =
        Mono.error(UnsupportedOperationException("VNPay capture unsupported"))

    override fun sale(body: PaymentRequest): Mono<Any> =
        props.takeIf {
            it.hashSecret.isNotBlank() &&
                it.returnUrl.isNotBlank() &&
                it.tmnCode.isNotBlank()
        }?.let { p ->
            val query = mapOf(
                "vnp_Version" to p.version,
                "vnp_Command" to "pay",
                "vnp_TmnCode" to p.tmnCode,
                "vnp_Amount" to (body.amount.toInt() * 100).toString(),
                "vnp_CurrCode" to p.currency,
                "vnp_TxnRef" to body.orderId,
                "vnp_OrderInfo" to (body.description ?: ("Create Payment for TxnRef=" + body.orderId)),
                "vnp_OrderType" to (body.orderType ?: "other"),
                "vnp_Locale" to p.locale,
                "vnp_ReturnUrl" to p.returnUrl,
                "vnp_IpAddr" to body.ipAddress,
                "vnp_CreateDate" to DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    .format(LocalDateTime.now())
            )
            val queryStr = query.entries
                .sortedBy { it.key }
                .joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, StandardCharsets.UTF_8)}" }
            val secureHash = hmacSHA512(p.hashSecret, queryStr)
            Mono.just("${p.baseUrl}?$queryStr&vnp_SecureHash=$secureHash")
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.payment.vn-pay.hash-secret' " +
                    "or 'provider.payment.vn-pay.tmn-code' " +
                    "or 'provider.payment.vn-pay.return-url' " +
                    "for VNPay configuration"
            )
        )


    override fun refund(body: PaymentRequest): Mono<Any> =
        props.takeIf {
            it.hashSecret.isNotBlank() &&
                it.tmnCode.isNotBlank()
        }?.let { p ->
            val refundBody = mapOf(
                "vnp_RequestId" to UUID.randomUUID().toString(),
                "vnp_Version" to p.version,
                "vnp_Command" to "refund",
                "vnp_TmnCode" to p.tmnCode,
                "vnp_TransactionType" to (body.transactionType ?: "02"),
                "vnp_TxnRef" to body.orderId,
                "vnp_Amount" to (body.amount.toInt() * 100).toString(),
                "vnp_OrderInfo" to ("Refund order " + body.orderId),
                "vnp_TransactionNo" to body.transactionNo,
                "vnp_TransactionDate" to body.transactionDate,
                "vnp_CreateBy" to (body.createBy ?: "system")
            )
            val query = refundBody.entries
                .sortedBy { it.key }
                .joinToString("&") { "${it.key}=${it.value}" }
            val secureHash = hmacSHA512(p.hashSecret, query)
            val finalBody = refundBody + ("vnp_SecureHash" to secureHash)
            webClient.post()
                .uri("${p.baseUrl}/merchant_webapi/api/transaction")
                .bodyValue(finalBody)
                .retrieve()
                .onStatus({ status -> status.isError }) { response ->
                    Mono.error(IllegalStateException("VNPay payment failed: ${response.statusCode()}"))
                }
                .bodyToMono(Map::class.java)
                .map { resp ->
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
    override fun void(body: PaymentRequest): Mono<Any> =
        Mono.error(UnsupportedOperationException("VNPay unsupported this action"))

    private fun hmacSHA512(key: String, data: String): String =
        Mac.getInstance("HmacSHA512").run {
            init(SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA512"))
            doFinal(data.toByteArray(StandardCharsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
        }
}
