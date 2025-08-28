package io.github.wliamp.pro.pay.data

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

    @Deprecated("Not supported by VNPay")
    override fun authorize(headers: Any, body: Any): Mono<Any> =
        Mono.error(UnsupportedOperationException("VNPay authorize-only unsupported"))

    @Deprecated("Not supported by VNPay")
    override fun capture(headers: Any, body: Any): Mono<Any> =
        Mono.error(UnsupportedOperationException("VNPay capture unsupported"))

    override fun sale(headers: Any, body: Any): Mono<Any> =
        props.takeIf {
            it.hashSecret.isNotBlank() &&
                it.returnUrl.isNotBlank() &&
                it.tmnCode.isNotBlank()
        }?.let { p ->
            @Suppress("UNCHECKED_CAST")
            (body as Map<String, String>).let { m ->
                val query = mapOf(
                    "vnp_Version" to p.version,
                    "vnp_Command" to "pay",
                    "vnp_TmnCode" to p.tmnCode,
                    "vnp_Amount" to (m["amount"]!!.toInt() * 100).toString(),
                    "vnp_CurrCode" to "VND",
                    "vnp_TxnRef" to m["orderId"]!!,
                    "vnp_OrderInfo" to m["description"]!!,
                    "vnp_OrderType" to (m["orderType"] ?: "other"),
                    "vnp_Locale" to (m["locale"] ?: "vn"),
                    "vnp_ReturnUrl" to p.returnUrl,
                    "vnp_IpAddr" to m["ipAddress"]!!,
                    "vnp_CreateDate" to DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
                )
                val queryStr = query.entries
                    .sortedBy { it.key }
                    .joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, StandardCharsets.UTF_8)}" }
                val secureHash = hmacSHA512(p.hashSecret, queryStr)
                Mono.just(mapOf("paymentUrl" to "${p.baseUrl}?$queryStr&vnp_SecureHash=$secureHash"))
            }
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.payment.vn-pay.hash-secret' " +
                    "or 'provider.payment.vn-pay.tmn-code' " +
                    "or 'provider.payment.vn-pay.return-url' " +
                    "for VNPay configuration"
            )
        )

    override fun refund(headers: Any, body: Any): Mono<Any> =
        props.takeIf {
            it.hashSecret.isNotBlank() &&
                it.tmnCode.isNotBlank()
        }?.let { p ->
            @Suppress("UNCHECKED_CAST")
            (body as Map<String, String>).let { m ->
                val refundBody = mapOf(
                    "vnp_RequestId" to UUID.randomUUID().toString(),
                    "vnp_Version" to p.version,
                    "vnp_Command" to "refund",
                    "vnp_TmnCode" to p.tmnCode,
                    "vnp_TransactionType" to (m["transactionType"] ?: "02"),
                    "vnp_TxnRef" to m["orderId"]!!,
                    "vnp_Amount" to (m["amount"]!!.toInt() * 100).toString(),
                    "vnp_OrderInfo" to "Refund order ${m["orderId"]}",
                    "vnp_TransactionNo" to m["transactionNo"]!!,
                    "vnp_TransactionDate" to m["transactionDate"]!!,
                    "vnp_CreateBy" to (m["createBy"] ?: "system")
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
            }
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.payment.vn-pay.hash-secret' " +
                    "or 'provider.payment.vn-pay.tmn-code' " +
                    "for VNPay configuration"
            )
        )


    override fun void(headers: Any, body: Any) = refund(headers, body)

    private fun hmacSHA512(key: String, data: String): String =
        Mac.getInstance("HmacSHA512").run {
            init(SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA512"))
            doFinal(data.toByteArray(StandardCharsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
        }
}
