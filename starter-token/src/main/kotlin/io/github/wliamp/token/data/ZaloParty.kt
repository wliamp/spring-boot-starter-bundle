package io.github.wliamp.token.data

import io.wliamp.token.config.TokenProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.collections.get

class ZaloParty(
    private val props: TokenProperties,
    private val webClient: WebClient
) : OauthParty {
    private val party = "zalo"

    override fun verify(token: String): Mono<Boolean> =
        if (props.zaloTokenInfoUrl.isBlank() || props.zaloAppId.isBlank()) {
            Mono.error(IllegalStateException("Zalo configuration missing"))
        } else {
            webClient.get()
                .uri("${props.zaloTokenInfoUrl}?access_token=$token")
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    Mono.just(IllegalStateException("Zalo verify failed: ${response.statusCode()}"))
                }
                .bodyToMono(Map::class.java)
                .map { response ->
                    response["id"] != null
                }
                .onErrorReturn(false)
        }

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        if (props.zaloTokenInfoUrl.isBlank() || props.zaloAppId.isBlank()) {
            Mono.error(IllegalStateException("Zalo configuration missing"))
        } else {
            webClient.get()
                .uri("${props.zaloTokenInfoUrl}?access_token=$token&fields=${props.zaloInfoFields}")
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    Mono.error(IllegalStateException("Zalo get information failed: ${response.statusCode()}"))
                }
                .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
        }
}
