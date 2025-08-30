package io.github.wliamp.pro.pay.impl

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.wliamp.pro.pay.config.PaymentProviderProps
import io.github.wliamp.pro.pay.req.ZaloPayRequest
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
            val appId = p.appId
            val appTransId = request.appTransId
            val appUser = request.appUser ?: ""
            val amount = (request.amount.toInt() * 100).toString()
            val appTime = System.currentTimeMillis().toString()
            val embedData = request.embedData ?: "{}"
            val item = request.item ?: "[]"
            val body = mutableMapOf(
                "app_id" to appId,
                "app_trans_id" to appTransId,
                "amount" to amount,
                "description" to (request.description ?: "Create Payment for app_trans_id=${request.appTransId}"),
                "url" to p.returnUrl,
                "cancel_url" to p.cancelUrl,
                "lang" to p.locale,
                "app_user" to appUser,
                "app_time" to appTime,
                "embed_data" to embedData,
                "item" to item,
                "mac" to hmacSHA256Sale(
                    p.macKey,
                    appId,
                    appTransId,
                    appUser,
                    amount,
                    appTime,
                    embedData,
                    item,
                )
            )
            request.orderCode?.let { body["order_code"] = it }
            request.paymentId?.let { body["payment_id"] = it }
            request.callbackUrl?.let { body["callback_url"] = it }
            request.preferredPaymentMethod?.let {
                body["preferred_payment_method"] = ObjectMapper().writeValueAsString(it)
            }
            webClient.post()
                .uri("${p.baseUrl}/v2/payment")
                .bodyValue(body)
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
            val appId = p.appId
            val zpTransId = request.zpTransId
            val amount = (request.amount.toInt() * 100).toString()
            val description = "Refund for order ${request.appTransId}"
            val timestamp = System.currentTimeMillis().toString()
            val body = mapOf(
                "app_id" to appId,
                "m_refund_id" to UUID.randomUUID().toString(),
                "zp_trans_id" to zpTransId,
                "amount" to amount,
                "timestamp" to timestamp,
                "description" to description,
                "mac" to hmacSHA256Refund(
                    p.macKey,
                    appId,
                    zpTransId,
                    amount,
                    description,
                    timestamp
                )
            )
            webClient.post()
                .uri("${p.baseUrl}/v2/refund")
                .bodyValue(body)
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

    private fun hmacSHA256Sale(
        macKey: String,
        appId: String,
        appTransId: String,
        appUser: String,
        amount: String,
        appTime: String,
        embedData: String,
        item: String
    ): String =
        hmacSHA256(macKey, listOf(appId, appTransId, appUser, amount, appTime, embedData, item).joinToString("|"))

    private fun hmacSHA256Refund(
        macKey: String,
        appId: String,
        zpTransId: String,
        amount: String,
        description: String,
        timestamp: String
    ): String =
        hmacSHA256(macKey, listOf(appId, zpTransId, amount, description, timestamp).joinToString("|"))

    private fun hmacSHA256(macKey: String, data: String): String =
        Mac.getInstance("HmacSHA256").run {
            init(SecretKeySpec(macKey.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
            doFinal(data.toByteArray(StandardCharsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
        }
}
