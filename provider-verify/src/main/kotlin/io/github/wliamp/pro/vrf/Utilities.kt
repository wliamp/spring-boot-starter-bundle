package io.github.wliamp.pro.vrf

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal fun WebClient.fetchPayload(
    method: HttpMethod,
    uri: String,
    provider: String,
    headers: Map<String, String> = emptyMap(),
    queryParams: Map<String, String> = emptyMap(),
    body: Any? = null
): Mono<Map<String, Any>> =
    this.method(method)
        .uri {
            it.path(uri)
            queryParams.forEach { (k, v) -> it.queryParam(k, v) }
            it.build()
        }
        .apply { headers.forEach { (k, v) -> this.header(k, v) } }
        .apply { body?.let { this.bodyValue(it) } }
        .retrieve()
        .onStatus({ it.isError }) { resp ->
            resp.bodyToMono(String::class.java)
                .flatMap { Mono.error(VerifyHttpException(provider, resp.statusCode().value(), it)) }
        }
        .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
        .onErrorMap {
            when (it) {
                is VerifyException -> it
                is java.net.ConnectException,
                is java.net.SocketTimeoutException,
                is org.springframework.web.reactive.function.client.WebClientRequestException ->
                    VerifyNetworkException(provider, it)

                is com.fasterxml.jackson.core.JsonProcessingException ->
                    VerifyParseException(provider, "Invalid JSON", it)

                is org.springframework.core.codec.DecodingException -> {
                    val cause = it.cause
                    if (cause is com.fasterxml.jackson.core.JsonProcessingException)
                        VerifyParseException(provider, "Invalid JSON", cause)
                    else VerifyParseException(provider, "Invalid JSON", it)
                }

                else -> VerifyUnexpectedException(provider, it)
            }
        }

