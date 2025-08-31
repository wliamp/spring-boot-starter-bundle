package io.github.wliamp.pro.pay.impl

import io.github.wliamp.pro.pay.config.PaymentProviderProps
import io.github.wliamp.pro.pay.cus.AuthorizeNetCus
import io.github.wliamp.pro.pay.sys.AuthorizeNetSys
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal class AuthorizeNetGtw internal constructor(
    private val props: PaymentProviderProps.AuthorizeNetProps,
    private val webClient: WebClient
) : IGtw<AuthorizeNetCus, AuthorizeNetSys> {
    private val provider = "authorizeNet"

    override fun authorize(cus: AuthorizeNetCus, sys: AuthorizeNetSys): Mono<Any> =
        getHostedPaymentToken("authOnlyTransaction", cus, sys)

    override fun sale(cus: AuthorizeNetCus, sys: AuthorizeNetSys): Mono<Any> =
        getHostedPaymentToken("authCaptureTransaction", cus, sys)

    override fun capture(cus: AuthorizeNetCus, sys: AuthorizeNetSys): Mono<Any> =
        requireAuthKeys().flatMap {
            val body = mapOf(
                "createTransactionRequest" to mapOf(
                    "merchantAuthentication" to merchantAuth(),
                    "transactionRequest" to mapOf(
                        "transactionType" to "priorAuthCaptureTransaction",
                        "amount" to cus.amount,
                        "refTransId" to sys.refTransId
                    )
                )
            )
            callJsonApi(body).map(::mapTxnResponse)
        }

    override fun refund(cus: AuthorizeNetCus, sys: AuthorizeNetSys): Mono<Any> =
        requireAuthKeys().flatMap {
            val body = mapOf(
                "createTransactionRequest" to mapOf(
                    "merchantAuthentication" to merchantAuth(),
                    "transactionRequest" to mapOf(
                        "transactionType" to "refundTransaction",
                        "amount" to cus.amount,
                        "refTransId" to sys.refTransId
                    )
                )
            )
            callJsonApi(body).map(::mapTxnResponse)
        }

    override fun void(cus: AuthorizeNetCus, sys: AuthorizeNetSys): Mono<Any> =
        requireAuthKeys().flatMap {
            val body = mapOf(
                "createTransactionRequest" to mapOf(
                    "merchantAuthentication" to merchantAuth(),
                    "transactionRequest" to mapOf(
                        "transactionType" to "voidTransaction",
                        "refTransId" to sys.refTransId
                    )
                )
            )
            callJsonApi(body).map(::mapTxnResponse)
        }

    private fun getHostedPaymentToken(
        transactionType: String,
        cus: AuthorizeNetCus,
        sys: AuthorizeNetSys
    ): Mono<Any> =
        requireAuthKeys().flatMap {
            val txnReq = mutableMapOf<String, Any?>(
                "transactionType" to transactionType,
                "amount" to cus.amount
            ).apply {
                if (!sys.orderId.isNullOrBlank() || !sys.description.isNullOrBlank()) {
                    this["order"] = mapOf(
                        "orderId" to sys.orderId,
                        "description" to (sys.description ?: ("Create Payment for orderId=" + sys.orderId))
                    )
                }
            }
            val body = mapOf(
                "getHostedPaymentPageRequest" to mapOf(
                    "merchantAuthentication" to merchantAuth(),
                    "transactionRequest" to txnReq,
                    "hostedPaymentSettings" to buildHostedPaymentSettings()
                )
            )
            callJsonApi(body).map { resp ->
                val token = resp["token"] as? String
                    ?: error("Authorize.Net Hosted Payment token is missing")
                mapOf(
                    "success" to true,
                    "provider" to provider,
                    "redirectUrl" to props.returnUrl,
                    "token" to token
                )
            }
        }

    private fun buildHostedPaymentSettings(): Map<String, Any> = mapOf(
        "setting" to listOf(
            mapOf(
                "settingName" to "hostedPaymentReturnOptions",
                "settingValue" to """
              {
                "showReceipt": true,
                "url": "${props.returnUrl}",
                "urlText": "Continue",
                "cancelUrl": "${props.cancelUrl}",
                "cancelUrlText": "Cancel"
              }
            """.trimIndent()
            )
        )
    )

    private fun callJsonApi(body: Any): Mono<Map<*, *>> =
        webClient.post()
            .uri(props.baseUrl)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(body)
            .retrieve()
            .onStatus({ it.isError }) { res ->
                res.bodyToMono(String::class.java).flatMap { body ->
                    Mono.error(IllegalStateException("Authorize.Net request failed: HTTP ${res.statusCode()} - $body"))
                }
            }
            .bodyToMono(Map::class.java)

    private fun merchantAuth(): Map<String, String> =
        mapOf("name" to props.apiLoginId, "transactionKey" to props.transactionKey)

    private fun requireAuthKeys(): Mono<Unit> =
        props.takeIf {
            it.apiLoginId.isNotBlank() &&
                it.transactionKey.isNotBlank() &&
                it.returnUrl.isNotBlank() &&
                it.cancelUrl.isNotBlank()
        }?.let { Mono.just(Unit) }
            ?: Mono.error(
                IllegalStateException(
                    "Missing parameter " +
                        "'provider.payment.authorize-net.api-login-id' " +
                        "or 'provider.payment.authorize-net.cancel-url' " +
                        "or 'provider.payment.authorize-net.return-url' " +
                        "or 'provider.payment.authorize-net.transaction-key' " +
                        "for AuthorizeNet configurations"
                )
            )

    private fun mapTxnResponse(resp: Map<*, *>) =
        (resp["transactionResponse"] as? Map<*, *>).let { txn ->
            val msgs = resp["messages"] as? Map<*, *>
            val responseCode = txn?.get("responseCode")?.toString()
            val transId = txn?.get("transId")?.toString()
            val resultCode = msgs?.get("resultCode")?.toString()
            val success = responseCode == "1" && resultCode == "Ok" && !transId.isNullOrBlank()
            mapOf(
                "success" to success,
                "provider" to provider,
                "transactionId" to transId,
                "authCode" to txn?.get("authCode"),
                "responseCode" to responseCode,
                "resultCode" to resultCode,
                "raw" to resp
            )
        }
}

