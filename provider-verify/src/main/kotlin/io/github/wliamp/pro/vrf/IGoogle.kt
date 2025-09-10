package io.github.wliamp.pro.vrf

import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal class IGoogle internal constructor(
    private val props: Properties.GoogleProps,
    private val webClient: WebClient
) : IOauth {
    private val oauth = Oauth.GOOGLE

    private val url = "${props.baseUrl}${props.uri}"

    override fun verify(token: String): Mono<Boolean> =
        props.takeIf { it.clientId.isNotBlank() }
            ?.let { p ->
                fetchPayload(token).map {
                    p.clientId == (it["aud"]?.toString()
                        ?: throw OauthParseException(oauth, "Missing 'aud' in response"))
                }
            }
            ?: Mono.error(
                OauthConfigException(
                    oauth,
                    "Missing 'provider.oauth.google.client-id'"
                )
            )

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        fetchPayload(token)

    private fun fetchPayload(token: String): Mono<Map<String, Any>> =
        webClient.get()
            .uri("${url}?id_token=$token")
            .retrieve()
            .onStatus({ it.isError }) { resp ->
                resp.bodyToMono(String::class.java)
                    .flatMap { Mono.error(OauthHttpException(oauth, resp.statusCode().value(), it)) }
            }
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .onErrorMap {
                when (it) {
                    is OauthException -> it
                    is java.net.ConnectException,
                    is java.net.SocketTimeoutException,
                    is org.springframework.web.reactive.function.client.WebClientRequestException ->
                        OauthNetworkException(oauth, it)

                    is com.fasterxml.jackson.core.JsonProcessingException ->
                        OauthParseException(oauth, "Invalid JSON", it)

                    is org.springframework.core.codec.DecodingException -> {
                        val cause = it.cause
                        if (cause is com.fasterxml.jackson.core.JsonProcessingException)
                            OauthParseException(oauth, "Invalid JSON", cause)
                        else OauthParseException(oauth, "Invalid JSON", it)
                    }

                    else -> OauthUnexpectedException(oauth, it)
                }
            }
}
