package io.github.wliamp.pro.pay.gtw

import io.github.wliamp.pro.pay.PaymentRequest
import io.github.wliamp.pro.pay.config.PaymentProviderProps
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal class ZaloPayGtw internal constructor(
    private val props: PaymentProviderProps.ZaloPayProps,
    private val webClient: WebClient
) : IGtw {
    private val provider = "zaloPay"

    @Deprecated(
        message = "Not supported by ZaloPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun authorize(body: PaymentRequest): Mono<Any> =
        Mono.error(UnsupportedOperationException("ZaloPay authorize-only unsupported"))

    @Deprecated(
        message = "Not supported by ZaloPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun capture(body: PaymentRequest): Mono<Any> =
        Mono.error(UnsupportedOperationException("ZaloPay capture unsupported"))

    override fun sale(body: PaymentRequest): Mono<Any> =
        props.takeIf {
            it.macKey.isNotBlank() &&
                it.returnUrl.isNotBlank() &&
                it.cancelUrl.isNotBlank()
        }?.let { p ->
            val orderInfo = body.description ?: "Create Payment for app_trans_id=${body.orderId}"
            val requestBody = mapOf(
                "app_id" to p.appId,
                "app_trans_id" to body.orderId,
                "amount" to (body.amount.toInt() * 100).toString(),
                "description" to orderInfo,
                "url" to p.returnUrl,
                "cancel_url" to p.cancelUrl,
                "lang" to p.locale,
                "mac" to generateMac(p, body.orderId, orderInfo)
            )
            webClient.post()
                .uri("${p.baseUrl}/v2/payment")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    Mono.error(IllegalStateException("ZaloPay sale failed: ${response.statusCode()}"))
                }
                .bodyToMono(Map::class.java)
                .map { response ->
                    mapOf(
                        "url" to response["pay_url"],
                        "orderId" to body.orderId
                    )
                }
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.payment.zalo-pay.mac-key', " +
                    "'provider.payment.zalo-pay.return-url' " +
                    "or 'provider.payment.zalo-pay.cancel-url' " +
                    "for ZaloPay configuration"
            )
        )

    override fun refund(body: PaymentRequest): Mono<Any> =
        props.takeIf {
            it.macKey.isNotBlank()
        }?.let { p ->
            val refundBody = mapOf(
                "app_id" to p.appId,
                "m_refund_id" to UUID.randomUUID().toString(),
                "zp_trans_id" to body.transactionNo,
                "amount" to (body.amount.toInt() * 100).toString(),
                "timestamp" to System.currentTimeMillis(),
                "description" to "Refund for order ${body.orderId}",
                "mac" to generateMac(p, body.transactionNo, "Refund for order ${body.orderId}")
            )
            webClient.post()
                .uri("${p.baseUrl}/v2/refund")
                .bodyValue(refundBody)
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    Mono.error(IllegalStateException("ZaloPay refund failed: ${response.statusCode()}"))
                }
                .bodyToMono(Map::class.java)
                .map { response ->
                    mapOf(
                        "success" to (response["return_code"] == 1),
                        "refundId" to response["refund_id"]
                    )
                }
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.payment.zalo-pay.mac-key' " +
                    "for ZaloPay configuration"
            )
        )

    @Deprecated(
        message = "Not supported by ZaloPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun void(body: PaymentRequest): Mono<Any> =
        Mono.error(UnsupportedOperationException("ZaloPay void unsupported"))

    private fun generateMac(p: PaymentProviderProps.ZaloPayProps, transId: String, description: String): String {
        val data = "${p.appId}|$transId|${description}|${System.currentTimeMillis()}"
        return hmacSHA256(p.macKey, data)
    }

    private fun hmacSHA256(key: String, data: String): String =
        Mac.getInstance("HmacSHA256").run {
            init(SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
            doFinal(data.toByteArray(StandardCharsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
        }
}
