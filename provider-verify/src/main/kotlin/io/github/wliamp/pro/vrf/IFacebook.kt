package io.github.wliamp.pro.vrf

import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.collections.get

internal class IFacebook internal constructor(
    private val props: Properties.FacebookProps,
    private val webClient: WebClient
) : IOauth {
    private val provider = "facebook"

    override fun verify(token: String): Mono<Boolean> =
        props.takeIf {
            it.appId.isNotBlank() &&
                it.accessToken.isNotBlank()
        }?.let { p ->
            webClient.get()
                .uri("${p.baseUrl}/debug_token?input_token=$token&access_token=${p.accessToken}")
                .retrieve()
                .onStatus({ it.isError }) {
                    Mono.error(IllegalStateException("Facebook verify failed: ${it.statusCode()}"))
                }
                .bodyToMono(Map::class.java)
                .map {
                    val data = it["data"] as? Map<*, *>
                    data?.get("app_id")?.toString() == p.appId
                }
                .onErrorReturn(false)
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'provider.oauth.facebook.app-id' " +
                    "or 'provider.oauth.facebook.access-token' " +
                    "for Facebook configuration"
            )
        )

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        webClient.get()
            .uri(props.fields.takeIf { it.isNotBlank() }
                ?.let { "${props.baseUrl}/me?access_token=$token&fields=${props.fields}" }
                ?: "${props.baseUrl}/me?access_token=$token"
            )
            .retrieve()
            .onStatus({ it.isError }) {
                Mono.error(IllegalStateException("Facebook get information failed: ${it.statusCode()}"))
            }
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
}
