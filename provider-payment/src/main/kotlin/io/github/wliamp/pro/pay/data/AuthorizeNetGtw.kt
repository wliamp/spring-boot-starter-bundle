package io.github.wliamp.pro.pay.data

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
        Mono.defer {
            val requestBody = buildTransactionRequestJson(transactionType, body)
            webClient.post()
                .uri(props.baseUrl)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map::class.java)
                .map { resp -> mapOf("success" to (resp["transactionResponse"] != null), "resp" to resp) }
        }

    private fun buildTransactionRequestJson(transactionType: String, body: Any): Map<String, Any> =
        @Suppress("UNCHECKED_CAST")
        (body as Map<String, Any>).let { p ->
            mapOf(
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
                            mapOf("settingName" to "testRequest", "settingValue" to "false")
                        )
                    )
                )
            )
        }
}

