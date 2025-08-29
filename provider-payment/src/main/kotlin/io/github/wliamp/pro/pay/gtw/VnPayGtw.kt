package io.github.wliamp.pro.pay.gtw

import io.github.wliamp.pro.pay.VnPayRequest
import io.github.wliamp.pro.pay.config.PaymentProviderProps
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

internal class VnPayGtw internal constructor(
    private val props: PaymentProviderProps.VnPayProps,
    private val webClient: WebClient
) : IGtw<VnPayRequest> {
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
            val query = mapOf(
                "vnp_Version" to p.version,
                "vnp_Command" to "pay",
                "vnp_TmnCode" to p.tmnCode,
                "vnp_Amount" to (request.vnpAmount.toInt() * 100).toString(),
                "vnp_CurrCode" to p.currency,
                "vnp_TxnRef" to request.vnpTxnRef,
                "vnp_OrderInfo" to (request.vnpOrderInfo ?: ("Create Payment for vnp_TxnRef=" + request.vnpTxnRef)),
                "vnp_OrderType" to (request.vnpOrderType ?: "other"),
                "vnp_Locale" to p.locale,
                "vnp_ReturnUrl" to p.returnUrl,
                "vnp_IpAddr" to request.vnpIpAddr,
                "vnp_CreateDate" to DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    .format(LocalDateTime.now())
            ).entries
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
            val body = mapOf(
                "vnp_RequestId" to UUID.randomUUID().toString(),
                "vnp_Version" to p.version,
                "vnp_Command" to "refund",
                "vnp_TmnCode" to p.tmnCode,
                "vnp_TransactionType" to (request.vnpTransactionType ?: "02"),
                "vnp_TxnRef" to request.vnpTxnRef,
                "vnp_Amount" to (request.vnpAmount.toInt() * 100).toString(),
                "vnp_OrderInfo" to ("Refund order " + request.vnpTxnRef),
                "vnp_TransactionNo" to request.vnpTransactionNo,
                "vnp_TransactionDate" to formatDate(request.vnpTransactionDate),
                "vnp_CreateBy" to (request.vnpCreateBy ?: "system")
            )
            webClient.post()
                .uri("${p.baseUrl}/merchant_webapi/api/transaction")
                .bodyValue(
                    body + ("vnp_SecureHash" to hmacSHA512(
                        p.hashSecret,
                        body.entries.sortedBy { it.key }.joinToString("&") { "${it.key}=${it.value}" }))
                )
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
    override fun void(request: VnPayRequest): Mono<Any> =
        Mono.error(UnsupportedOperationException("VNPay unsupported this action"))

    private fun hmacSHA512(key: String, data: String): String =
        Mac.getInstance("HmacSHA512").run {
            init(SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA512"))
            doFinal(data.toByteArray(StandardCharsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
        }

    private fun formatDate(input: Any): String =
        (when (input) {
            is String -> runCatching {
                LocalDateTime.parse(input, DateTimeFormatter.ISO_DATE_TIME)
            }.getOrElse {
                LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }

            is Long -> LocalDateTime.ofInstant(Instant.ofEpochMilli(input), ZoneId.systemDefault())
            is LocalDateTime -> input
            else -> throw IllegalArgumentException("Unsupported date type for VNPay: ${input::class}")
        }).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
}
