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
        props.takeIf {
            it.facebookAppId.isNotBlank() &&
                it.facebookTokenInfoUrl.isNotBlank() &&
                it.facebookAppAccessToken.isNotBlank()
        }?.let {
            webClient.get()
                .uri("${it.facebookTokenInfoUrl}?input_token=$token&access_token=${it.facebookAppAccessToken}")
                .retrieve()
                .onStatus({ status -> status.isError }) { response ->
                    Mono.error(IllegalStateException("Facebook verify failed: ${response.statusCode()}"))
                }
                .bodyToMono(Map::class.java)
                .map { response ->
                    val data = response["data"] as? Map<*, *>
                    data?.get("app_id")?.toString() == it.facebookAppId
                }
                .onErrorReturn(false)
        } ?: Mono.error(IllegalStateException("Facebook configuration missing"))

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        props.takeIf {
            it.facebookTokenInfoUrl.isNotBlank() &&
                it.facebookAppAccessToken.isNotBlank()
        }?.let {
            webClient.get()
                .uri("https://graph.facebook.com/me?access_token=$token&fields=${it.facebookInfoFields}")
                .retrieve()
                .onStatus({ status -> status.isError }) { response ->
                    Mono.error(IllegalStateException("Facebook get information failed: ${response.statusCode()}"))
                }
                .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
        } ?: Mono.error(IllegalStateException("Facebook configuration missing"))
}
