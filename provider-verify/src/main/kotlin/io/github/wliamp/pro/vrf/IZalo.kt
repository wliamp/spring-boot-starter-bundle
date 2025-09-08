package io.github.wliamp.pro.vrf

import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.collections.get

internal class IZalo internal constructor(
    private val props: Properties.ZaloProps,
    private val webClient: WebClient
) : IOauth {
    private val provider = "zalo"

    override fun verify(token: String): Mono<Boolean> =
        webClient.get()
            .uri("${props.baseUrl}?access_token=$token")
            .retrieve()
            .onStatus({ it.isError }) {
                Mono.error(IllegalStateException("Zalo verify failed: ${it.statusCode()}"))
            }
            .bodyToMono(Map::class.java)
            .map { it["id"] != null }
            .onErrorReturn(false)


    override fun getInfo(token: String): Mono<Map<String, Any>> =
        webClient.get()
            .uri(props.fields.takeIf { it.isNotBlank() }
                ?.let { "${props.baseUrl}?access_token=$token&fields=${props.fields}" }
                ?: "${props.baseUrl}?access_token=$token"
            )
            .retrieve()
            .onStatus({ it.isError }) {
                Mono.error(IllegalStateException("Zalo get information failed: ${it.statusCode()}"))
            }
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
}
