package io.github.wliamp.pro.pay.impl

import io.github.wliamp.pro.pay.config.PaymentProviderProps
import io.github.wliamp.pro.pay.cus.ZaloPayCus
import io.github.wliamp.pro.pay.sys.ZaloPaySys
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal class ZaloPayGtw internal constructor(
    private val props: PaymentProviderProps.ZaloPayProps,
    private val webClient: WebClient
) : IGtw<ZaloPayCus, ZaloPaySys> {
    private val provider = "zaloPay"

    @Deprecated(
        message = "Not supported by ZaloPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun authorize(cus: ZaloPayCus, sys: ZaloPaySys): Mono<Any> =
        Mono.error(UnsupportedOperationException("ZaloPay authorize-only unsupported"))

    @Deprecated(
        message = "Not supported by ZaloPay",
        level = DeprecationLevel.HIDDEN
    )
    override fun capture(cus: ZaloPayCus, sys: ZaloPaySys): Mono<Any> =
        Mono.error(UnsupportedOperationException("ZaloPay capture unsupported"))

    override fun sale(cus: ZaloPayCus, sys: ZaloPaySys): Mono<Any> =
        props.takeIf {
            (it.appId > 0) &&
                it.macKey.isNotBlank()
        }?.let { p ->
            val appId = p.appId
            val appUser = sys.appUser ?: ""
            val appTransId = sys.appTransId
            val appTime = System.currentTimeMillis().toString()
            val amount = cus.amount
            val item = cus.item ?: "[]"
            val description = cus.description ?: "Payment for the order #${appTransId}"
            val embedData = cus.embedData ?: "{}"
            val body = mutableMapOf(
                "app_id" to appId,
                "app_user" to appUser,
                "app_trans_id" to appTransId,
                "app_time" to appTime,
                "expire_duration_seconds" to p.expireDurationSeconds,
                "amount" to amount,
                "item" to item,
                "description" to description,
                "embed_data" to embedData,
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
            cus.bankCode?.let { body["bank_code"] = it }
            cus.deviceInfo?.let { body["device_info"] = it }
            p.subAppId?.let { body["sub_app_id"] = it }
            sys.callbackUrl?.let { body["callback_url"] = it }
            webClient.post()
                .uri("${p.baseUrl}${p.saleUri}")
                .bodyValue(body)
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    Mono.error(IllegalStateException("ZaloPay sale failed: ${response.statusCode()}"))
                }
                .bodyToMono(Map::class.java)
                .map { resp ->
                    mapOf(
                        "resultCode" to resp["return_code"],
                        "resultMsg" to resp["return_message"],
                        "subResultCode" to resp["sub_return_code"],
                        "subResultMsg" to resp["sub_return_message"],
                        "paymentUrl" to resp["order_url"],
                        "zpTransToken" to resp["zp_trans_token"],
                        "orderToken" to resp["order_token"],
                        "qrCode" to resp["qr_code"]
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

    override fun refund(cus: ZaloPayCus, sys: ZaloPaySys): Mono<Any> =
        props.takeIf {
            (it.appId > 0) &&
                it.macKey.isNotBlank()
        }?.let { p ->
            val appId = p.appId
            val mRefundId =

            val zpTransId = sys.zpTransToken
            val amount = cus.amount
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
                .uri("${p.baseUrl}${p.refundUri}")
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
    override fun void(cus: ZaloPayCus, sys: ZaloPaySys): Mono<Any> =
        Mono.error(UnsupportedOperationException("ZaloPay void unsupported"))

    private fun hmacSHA256Sale(
        macKey: String,
        appId: Int,
        appTransId: String,
        appUser: String,
        amount: Long,
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
