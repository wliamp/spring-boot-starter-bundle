package io.github.wliamp.pro.vrf

import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal class IGoogle internal constructor(
    private val props: Properties.GoogleProps,
    private val webClient: WebClient
) : IOauth {
    private val provider = "google"

    override fun verify(token: String): Mono<Boolean> =
        props.takeIf { it.clientId.isNotBlank() }
            ?.let { p ->
                fetchGooglePayload(token).map {
                    p.clientId == (it["aud"]?.toString()
                        ?: throw GoogleParseException("Missing 'aud' in Google response"))
                }
            }
            ?: Mono.error(
                GoogleConfigException(
                    "Missing " +
                        "'provider.oauth.google.client-id' " +
                        "for Google configuration"
                )
            )

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        fetchGooglePayload(token)

    private fun fetchGooglePayload(token: String): Mono<Map<String, Any>> =
        webClient.get()
            .uri("${props.baseUrl}?id_token=$token")
            .retrieve()
            .onStatus({ it.isError }) { resp ->
                resp.bodyToMono(String::class.java)
                    .flatMap { Mono.error(GoogleHttpException(resp.statusCode().value(), it)) }
            }
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .onErrorMap {
                when (it) {
                    is GoogleOauthException -> it
                    is java.net.ConnectException,
                    is java.net.SocketTimeoutException -> GoogleNetworkException(it)

                    is com.fasterxml.jackson.core.JsonProcessingException -> GoogleParseException("Invalid JSON", it)
                    else -> GoogleUnexpectedException(it)
                }
            }
}
