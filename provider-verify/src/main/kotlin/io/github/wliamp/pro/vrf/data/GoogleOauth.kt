package io.github.wliamp.pro.vrf.data

import io.github.wliamp.pro.vrf.config.VerifyProviderProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.collections.get

class GoogleOauth(
    private val props: VerifyProviderProperties.GoogleProps,
    private val webClient: WebClient
) : IOauth {
    private val provider = "google"

    override fun verify(token: String): Mono<Boolean> =
        props.takeIf {
            it.clientId.isNotBlank() &&
                it.tokenInfoUrl.isNotBlank()
        }?.let {
            webClient.get()
                .uri("${props.tokenInfoUrl}?id_token=$token")
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    Mono.error(IllegalStateException("Google verify failed: ${response.statusCode()}"))
                }
                .bodyToMono(Map::class.java)
                .map { response ->
                    val aud = response["aud"]?.toString()
                    aud == props.clientId
                }
                .onErrorReturn(false)
        } ?: Mono.error(IllegalStateException("Google configuration missing"))

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        props.tokenInfoUrl
            .takeIf { it.isNotBlank() }
            ?.let {
                webClient.get()
                    .uri("$it?id_token=$token")
                    .retrieve()
                    .onStatus({ it.isError }) { response ->
                        Mono.error(IllegalStateException("Google get information failed: ${response.statusCode()}"))
                    }
                    .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
            } ?: Mono.error(IllegalStateException("Google configuration missing"))
}
