package io.github.wliamp.pro.vrf.data

import io.github.wliamp.pro.vrf.config.VerifyProviderProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.collections.get

class FacebookOauth(
    private val props: VerifyProviderProperties,
    private val webClient: WebClient
) : IOauth {
    private val provider = "facebook"

    override fun verify(token: String): Mono<Boolean> =
        if (props.facebookAppId.isBlank() || props.facebookTokenInfoUrl.isBlank() || props.facebookAppAccessToken.isBlank()) Mono.error(
            IllegalStateException("Facebook configuration missing")
        ) else webClient.get()
            .uri("${props.facebookTokenInfoUrl}?input_token=$token&access_token=${props.facebookAppAccessToken}")
            .retrieve()
            .onStatus({ it.isError }) { response ->
                Mono.just(IllegalStateException("Facebook verify failed: ${response.statusCode()}"))
            }
            .bodyToMono(Map::class.java)
            .map { response ->
                val data = response["data"] as? Map<*, *>
                data?.get("app_id")?.toString() == props.facebookAppId
            }
            .onErrorReturn(false)

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        if (props.facebookTokenInfoUrl.isBlank() || props.facebookAppAccessToken.isBlank()) Mono.error(
            IllegalStateException("Facebook configuration missing")
        ) else webClient.get()
            .uri("https://graph.facebook.com/me?access_token=$token&fields=${props.facebookInfoFields}")
            .retrieve()
            .onStatus({ it.isError }) { response ->
                Mono.error(IllegalStateException("Facebook get information failed: ${response.statusCode()}"))
            }
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
}
