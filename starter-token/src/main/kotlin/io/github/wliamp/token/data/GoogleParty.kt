package io.github.wliamp.token.data

import io.wliamp.token.config.TokenProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.collections.get

class GoogleParty(
    private val props: TokenProperties,
    private val webClient: WebClient
) : OauthParty {
    private val party = "google"

    override fun verify(token: String): Mono<Boolean> =
        if (props.googleClientId.isBlank() || props.googleTokenInfoUrl.isBlank()) {
            Mono.error(IllegalStateException("Google configuration missing"))
        } else {
            webClient.get()
                .uri("${props.googleTokenInfoUrl}?id_token=$token")
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    Mono.error(IllegalStateException("Google verify failed: ${response.statusCode()}"))
                }
                .bodyToMono(Map::class.java)
                .map { response ->
                    val aud = response["aud"]?.toString()
                    aud == props.googleClientId
                }
                .onErrorReturn(false)
        }

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        if (props.googleTokenInfoUrl.isBlank()) {
            Mono.error(IllegalStateException("Google config missing"))
        } else {
            webClient.get()
                .uri("${props.googleTokenInfoUrl}?id_token=$token")
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    Mono.error(IllegalStateException("Google getInfo failed: ${response.statusCode()}"))
                }
                .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
        }
}
