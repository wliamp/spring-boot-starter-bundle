package io.github.wliamp.pro.vrf.data

import io.github.wliamp.pro.vrf.config.VerifyProviderProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.collections.get

class ZaloOauth(
    private val props: VerifyProviderProperties.ZaloProps,
    private val webClient: WebClient
) : IOauth {
    private val provider = "zalo"

    override fun verify(token: String): Mono<Boolean> =
        props.takeIf {
            it.tokenInfoUrl.isNotBlank()
                && it.appId.isNotBlank()
        }?.let {
            webClient.get()
                .uri("${props.tokenInfoUrl}?access_token=$token")
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    Mono.error(IllegalStateException("Zalo verify failed: ${response.statusCode()}"))
                }
                .bodyToMono(Map::class.java)
                .map { response -> response["id"] != null }
                .onErrorReturn(false)
        } ?: Mono.error(IllegalStateException("Zalo configuration missing"))

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        props.takeIf {
            it.tokenInfoUrl.isNotBlank() &&
                it.appId.isNotBlank()
        }?.let {
            webClient.get()
                .uri("${props.tokenInfoUrl}?access_token=$token&fields=${props.infoFields}")
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    Mono.error(IllegalStateException("Zalo get information failed: ${response.statusCode()}"))
                }
                .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
        } ?: Mono.error(IllegalStateException("Zalo configuration missing"))
}
