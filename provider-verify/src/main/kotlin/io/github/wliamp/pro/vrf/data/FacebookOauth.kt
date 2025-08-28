package io.github.wliamp.pro.vrf.data

import io.github.wliamp.pro.vrf.config.VerifyProviderProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.collections.get

class FacebookOauth(
    private val props: VerifyProviderProperties.FacebookProps,
    private val webClient: WebClient
) : IOauth {
    private val provider = "facebook"

    override fun verify(token: String): Mono<Boolean> =
        props.takeIf {
            it.appId.isNotBlank() &&
                it.tokenInfoUrl.isNotBlank() &&
                it.appAccessToken.isNotBlank()
        }?.let {
            webClient.get()
                .uri("${it.tokenInfoUrl}?input_token=$token&access_token=${it.appAccessToken}")
                .retrieve()
                .onStatus({ status -> status.isError }) { response ->
                    Mono.error(IllegalStateException("Facebook verify failed: ${response.statusCode()}"))
                }
                .bodyToMono(Map::class.java)
                .map { response ->
                    val data = response["data"] as? Map<*, *>
                    data?.get("app_id")?.toString() == it.appId
                }
                .onErrorReturn(false)
        } ?: Mono.error(IllegalStateException("Facebook configuration missing"))

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        props.takeIf {
            it.infoFields.isNotBlank()
        }?.let {
            webClient.get()
                .uri("https://graph.facebook.com/me?access_token=$token&fields=${it.infoFields}")
                .retrieve()
                .onStatus({ status -> status.isError }) { response ->
                    Mono.error(IllegalStateException("Facebook get information failed: ${response.statusCode()}"))
                }
                .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
        } ?: Mono.error(IllegalStateException("Facebook configuration missing"))
}
