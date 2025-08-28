package io.github.wliamp.pro.vrf.oauth

import io.github.wliamp.pro.vrf.config.VerifyProviderProps
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.collections.get

internal class GoogleOauth internal constructor(
    private val props: VerifyProviderProps.GoogleProps,
    private val webClient: WebClient
) : IOauth {
    private val provider = "google"

    override fun verify(token: String): Mono<Boolean> =
        props.takeIf { it.clientId.isNotBlank() }
            ?.let {
                webClient.get()
                    .uri("${it.baseUrl}?id_token=$token")
                    .retrieve()
                    .onStatus({ it.isError }) { response ->
                        Mono.error(IllegalStateException("Google verify failed: ${response.statusCode()}"))
                    }
                    .bodyToMono(Map::class.java)
                    .map { response ->
                        val aud = response["aud"]?.toString()
                        aud == it.clientId
                    }
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
            .onStatus({ it.isError }) { response ->
                Mono.error(IllegalStateException("Google get information failed: ${response.statusCode()}"))
            }
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
}
