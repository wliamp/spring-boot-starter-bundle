package io.github.wliamp.pro.pay

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal inline fun <reified T : Number> String.toNumber(): T? =
    when (T::class) {
        Int::class -> this.toIntOrNull() as T?
        Long::class -> this.toLongOrNull() as T?
        Float::class -> this.toFloatOrNull() as T?
        Double::class -> this.toDoubleOrNull() as T?
        else -> null
    }

internal fun MutableMap<String, Any>.optional(key: String, value: Any?) {
    when (value) {
        null -> return
        is String -> if (value.isBlank()) return
        is Long -> if (value == 0L) return
    }
    this[key] = value
}

internal fun generateCode(size: Int): String =
    UUID.randomUUID().toString().replace("-", "")
        .take(size)

internal fun formatDate(input: Any?, pattern: String): String =
    when (input) {
        is String -> LocalDateTime.parse(input, DateTimeFormatter.ofPattern(pattern))
        is Long -> LocalDateTime.ofInstant(Instant.ofEpochMilli(input), ZoneId.systemDefault())
        is LocalDateTime -> input
        else -> throw IllegalArgumentException("Unsupported date type: ${input!!::class}")
    }.format(DateTimeFormatter.ofPattern(pattern))

internal fun hmac(code: String, key: String, data: String): String =
    Mac.getInstance("Hmac${code}").run {
        init(SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "Hmac${code}"))
        doFinal(data.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

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
                .flatMap { Mono.error(PaymentHttpException(provider, resp.statusCode().value(), it)) }
        }
        .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
        .onErrorMap {
            when (it) {
                is PaymentException -> it
                is java.net.ConnectException,
                is java.net.SocketTimeoutException,
                is org.springframework.web.reactive.function.client.WebClientRequestException ->
                    PaymentNetworkException(provider, it)

                is com.fasterxml.jackson.core.JsonProcessingException ->
                    PaymentParseException(provider, "Invalid JSON", it)

                is org.springframework.core.codec.DecodingException -> {
                    val cause = it.cause
                    if (cause is com.fasterxml.jackson.core.JsonProcessingException)
                        PaymentParseException(provider, "Invalid JSON", cause)
                    else PaymentParseException(provider, "Invalid JSON", it)
                }

                else -> PaymentUnexpectedException(provider, it)
            }
        }
