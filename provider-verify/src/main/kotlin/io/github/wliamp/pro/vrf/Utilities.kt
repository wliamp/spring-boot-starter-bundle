package io.github.wliamp.pro.vrf

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal fun WebClient.fetchPayload(
    method: HttpMethod,
    uri: String,
    oauth: Oauth
): Mono<Map<String, Any>> =
    this.method(method)
        .uri(uri)
        .retrieve()
        .onStatus({ it.isError }) { resp ->
            resp.bodyToMono(String::class.java)
                .flatMap {
                    Mono.error(VerifyHttpException(oauth, resp.statusCode().value(), it))
                }
        }
        .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
        .onErrorMap {
            when (it) {
                is VerifyException -> it
                is java.net.ConnectException,
                is java.net.SocketTimeoutException,
                is org.springframework.web.reactive.function.client.WebClientRequestException ->
                    VerifyNetworkException(oauth, it)

                is com.fasterxml.jackson.core.JsonProcessingException ->
                    VerifyParseException(oauth, "Invalid JSON", it)

                is org.springframework.core.codec.DecodingException -> {
                    val cause = it.cause
                    if (cause is com.fasterxml.jackson.core.JsonProcessingException)
                        VerifyParseException(oauth, "Invalid JSON", cause)
                    else VerifyParseException(oauth, "Invalid JSON", it)
                }

                else -> VerifyUnexpectedException(oauth, it)
            }
        }
