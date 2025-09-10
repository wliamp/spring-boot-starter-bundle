package io.github.wliamp.pro.vrf

import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal class IZalo internal constructor(
    private val props: Properties.ZaloProps,
    private val webClient: WebClient
) : IOauth {
    private val oauth = Oauth.ZALO

    override fun verify(token: String): Mono<Boolean> =
        fetchPayload("${props.baseUrl}?access_token=$token")
            .map {
                it["id"]?.toString()
                    ?: throw OauthParseException(oauth, "Missing 'id' in response")
                true
            }

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        fetchPayload(
            props.fields.takeIf { it.isNotBlank() }
                ?.let { "${props.baseUrl}?access_token=$token&fields=$it" }
                ?: "${props.baseUrl}?access_token=$token"
        )

    private fun fetchPayload(uri: String): Mono<Map<String, Any>> =
        webClient.get()
            .uri(uri)
            .retrieve()
            .onStatus({ it.isError }) { resp ->
                resp.bodyToMono(String::class.java)
                    .flatMap {
                        Mono.error(OauthHttpException(oauth, resp.statusCode().value(), it))
                    }
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
