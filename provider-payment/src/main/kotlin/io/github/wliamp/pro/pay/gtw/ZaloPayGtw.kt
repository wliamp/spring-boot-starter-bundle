package io.github.wliamp.pro.pay.gtw

import io.github.wliamp.pro.pay.ZaloPayRequest
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
) : IGtw<ZaloPayRequest> {
    private val provider = "zaloPay"

    @Deprecated(
        message = "Not supported by ZaloPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun authorize(request: ZaloPayRequest): Mono<Any> =
        Mono.error(UnsupportedOperationException("ZaloPay authorize-only unsupported"))

    @Deprecated(
        message = "Not supported by ZaloPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun capture(request: ZaloPayRequest): Mono<Any> =
        Mono.error(UnsupportedOperationException("ZaloPay capture unsupported"))

    override fun sale(request: ZaloPayRequest): Mono<Any> =
        props.takeIf {
            it.appId.isNotBlank() &&
                it.macKey.isNotBlank() &&
                it.returnUrl.isNotBlank() &&
                it.cancelUrl.isNotBlank()
        }?.let { p ->
            val orderInfo = request.description ?: "Create Payment for app_trans_id=${request.appTransId}"
            webClient.post()
                .uri("${p.baseUrl}/v2/payment")
                .bodyValue(
                    mapOf(
                        "app_id" to p.appId,
                        "app_trans_id" to request.appTransId,
                        "amount" to (request.amount.toInt() * 100).toString(),
                        "description" to orderInfo,
                        "url" to p.returnUrl,
                        "cancel_url" to p.cancelUrl,
                        "lang" to p.locale,
                        "mac" to hmacSHA256(p.appId, p.macKey, request.appTransId, orderInfo)
                    )
                )
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    Mono.error(IllegalStateException("ZaloPay sale failed: ${response.statusCode()}"))
                }
                .bodyToMono(Map::class.java)
                .map { response ->
                    mapOf(
                        "url" to response["pay_url"],
                        "orderId" to request.appTransId
                    )
                }
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.payment.zalo-pay.app-id' " +
                    "or 'provider.payment.zalo-pay.mac-key' " +
                    "or 'provider.payment.zalo-pay.return-url' " +
                    "or 'provider.payment.zalo-pay.cancel-url' " +
                    "for ZaloPay configuration"
            )
        )

    override fun refund(request: ZaloPayRequest): Mono<Any> =
        props.takeIf {
            it.appId.isNotBlank() &&
                it.macKey.isNotBlank()
        }?.let { p ->
            webClient.post()
                .uri("${p.baseUrl}/v2/refund")
                .bodyValue(
                    mapOf(
                        "app_id" to p.appId,
                        "m_refund_id" to UUID.randomUUID().toString(),
                        "zp_trans_id" to request.zpTransId,
                        "amount" to (request.amount.toInt() * 100).toString(),
                        "timestamp" to System.currentTimeMillis(),
                        "description" to "Refund for order ${request.appTransId}",
                        "mac" to hmacSHA256(
                            p.appId,
                            p.macKey,
                            request.zpTransId,
                            "Refund for order ${request.appTransId}"
                        )
                    )
                )
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
                    "'provider.payment.zalo-pay.app-id' " +
                    "or 'provider.payment.zalo-pay.mac-key' " +
                    "for ZaloPay configuration"
            )
        )

    @Deprecated(
        message = "Not supported by ZaloPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun void(request: ZaloPayRequest): Mono<Any> =
        Mono.error(UnsupportedOperationException("ZaloPay void unsupported"))

    private fun hmacSHA256(appId: String, macKey: String, transId: String, description: String): String =
        Mac.getInstance("HmacSHA256").run {
            init(SecretKeySpec(macKey.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
            doFinal(
                "${appId}|$transId|${description}|${System.currentTimeMillis()}"
                    .toByteArray(StandardCharsets.UTF_8)
            )
                .joinToString("") { "%02x".format(it) }
        }
}
