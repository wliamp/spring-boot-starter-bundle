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
    private val props: PaymentProviderProps,
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
        @Suppress("UNCHECKED_CAST")
        (body as Map<String, String>).let { p ->
            val query = mapOf(
                "vnp_Version" to props.vnPay.version,
                "vnp_Command" to "pay",
                "vnp_TmnCode" to props.vnPay.tmnCode,
                "vnp_Amount" to (p["amount"]!!.toInt() * 100).toString(),
                "vnp_CurrCode" to "VND",
                "vnp_TxnRef" to p["orderId"]!!,
                "vnp_OrderInfo" to p["description"]!!,
                "vnp_OrderType" to (p["orderType"] ?: "other"),
                "vnp_Locale" to (p["locale"] ?: "vn"),
                "vnp_ReturnUrl" to props.vnPay.returnUrl,
                "vnp_IpAddr" to p["ipAddress"]!!,
                "vnp_CreateDate" to DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
            )
            val queryStr = query.entries
                .sortedBy { it.key }
                .joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, StandardCharsets.UTF_8)}" }
            val secureHash = hmacSHA512(props.vnPay.secretKey, queryStr)
            Mono.just(
                mapOf("paymentUrl" to "${props.vnPay.baseUrl}?$queryStr&vnp_SecureHash=$secureHash")
            )
        }

    override fun refund(headers: Any, body: Any): Mono<Any> =
        @Suppress("UNCHECKED_CAST")
        (body as Map<String, String>).let { p ->
            val refundBody = mapOf(
                "vnp_RequestId" to UUID.randomUUID().toString(),
                "vnp_Version" to props.vnPay.version,
                "vnp_Command" to "refund",
                "vnp_TmnCode" to props.vnPay.tmnCode,
                "vnp_TransactionType" to (p["transactionType"] ?: "02"),
                "vnp_TxnRef" to p["orderId"]!!,
                "vnp_Amount" to (p["amount"]!!.toInt() * 100).toString(),
                "vnp_OrderInfo" to "Refund order ${p["orderId"]}",
                "vnp_TransactionNo" to p["transactionNo"]!!,
                "vnp_TransactionDate" to p["transactionDate"]!!,
                "vnp_CreateBy" to (p["createBy"] ?: "system")
            )
            val query = refundBody.entries.sortedBy { it.key }
                .joinToString("&") { "${it.key}=${it.value}" }
            val secureHash = hmacSHA512(props.vnPay.secretKey, query)
            val finalBody = refundBody + ("vnp_SecureHash" to secureHash)
            webClient.post()
                .uri("${props.vnPay.baseUrl}/merchant_webapi/api/transaction")
                .bodyValue(finalBody)
                .retrieve()
                .bodyToMono(Map::class.java)
                .map { resp -> mapOf("success" to (resp["vnp_ResponseCode"] == "00"), "resp" to resp) }
        }

    override fun void(headers: Any, body: Any) = refund(headers, body)

    private fun hmacSHA512(key: String, data: String): String =
        Mac.getInstance("HmacSHA512").run {
            init(SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA512"))
            doFinal(data.toByteArray(StandardCharsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
        }
}
