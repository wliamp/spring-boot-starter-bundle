package io.github.wliamp.pro.pay.gtw

import io.github.wliamp.pro.pay.config.PaymentProviderProps
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal class AuthorizeNetGtw internal constructor(
    private val props: PaymentProviderProps.AuthorizeNetProps,
    private val webClient: WebClient
) : IGtw {
    private val provider = "authorizeNet"

    override fun authorize(headers: Any, body: Any): Mono<Any> =
        processTransaction("authOnlyTransaction", body)

    override fun capture(headers: Any, body: Any): Mono<Any> =
        processTransaction("priorAuthCaptureTransaction", body)

    override fun sale(headers: Any, body: Any): Mono<Any> =
        processTransaction("authCaptureTransaction", body)

    override fun refund(headers: Any, body: Any): Mono<Any> =
        processTransaction("refundTransaction", body)

    override fun void(headers: Any, body: Any): Mono<Any> =
        processTransaction("voidTransaction", body)

    private fun processTransaction(transactionType: String, body: Any): Mono<Any> =
        props.takeIf {
            it.apiLoginId.isNotBlank() &&
                it.transactionKey.isNotBlank()
        }?.let {
            Mono.defer {
                @Suppress("UNCHECKED_CAST")
                val p = body as Map<String, Any>
                val requestBody = mapOf(
                    "createTransactionRequest" to mapOf(
                        "merchantAuthentication" to mapOf(
                            "name" to props.apiLoginId,
                            "transactionKey" to props.transactionKey
                        ),
                        "transactionRequest" to mapOf(
                            "transactionType" to transactionType,
                            "amount" to p["amount"]!!,
                            "payment" to mapOf(
                                "creditCard" to mapOf(
                                    "cardNumber" to p["cardNumber"]!!,
                                    "expirationDate" to p["expirationDate"]!!,
                                    "cardCode" to p["cardCode"]!!
                                )
                            ),
                            "order" to mapOf(
                                "invoiceNumber" to p["invoiceNumber"]!!,
                                "description" to (p["description"] as? String ?: "")
                            ),
                            "transactionSettings" to listOf(
                                mapOf(
                                    "settingName" to "testRequest",
                                    "settingValue" to "false"
                                )
                            )
                        )
                    )
                )
                webClient.post()
                    .uri(props.baseUrl)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus({ status -> status.isError }) { response ->
                        Mono.error(IllegalStateException("AuthorizeNet payment failed: ${response.statusCode()}"))
                    }
                    .bodyToMono(Map::class.java)
                    .map { resp ->
                        mapOf(
                            "success" to (resp["transactionResponse"] != null),
                            "resp" to resp
                        )
                    }
            }
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.payment.authorize-net.api-login-id' " +
                    "or 'provider.payment.authorize-net.transaction-key' " +
                    "for VNPay configuration"
            )
        )
}

