package io.github.wliamp.pro.vrf

import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.collections.get

internal class IGoogle internal constructor(
    private val props: Properties.GoogleProps,
    private val webClient: WebClient
) : IOauth {
    private val provider = "google"

    override fun verify(token: String): Mono<Boolean> =
        props.takeIf { it.clientId.isNotBlank() }
            ?.let { p ->
                webClient.get()
                    .uri("${p.baseUrl}?id_token=$token")
                    .retrieve()
                    .onStatus({ it.isError }) {
                        Mono.error(IllegalStateException("Google verify failed: ${it.statusCode()}"))
                    }
                    .bodyToMono(Map::class.java)
                    .map { it["aud"]?.toString() == p.clientId }
                    .onErrorReturn(false)
            } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.oauth.google.client-id' " +
                    "for Google configuration"
            )
        )

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        webClient.get()
            .uri("${props.baseUrl}?id_token=$token")
            .retrieve()
            .onStatus({ it.isError }) {
                Mono.error(IllegalStateException("Google get information failed: ${it.statusCode()}"))
            }
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
}
