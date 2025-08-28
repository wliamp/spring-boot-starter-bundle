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
                it.accessToken.isNotBlank()
        }?.let {
            webClient.get()
                .uri("${it.baseUrl}/debug_token?input_token=$token&access_token=${it.accessToken}")
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
        } ?: Mono.error(
            IllegalStateException(
                "Missing parameter " +
                    "'verify.facebook.app-id' " +
                    "or 'verify.facebook.access-token' " +
                    "for Facebook configuration"
            )
        )

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        webClient.get()
            .uri(props.fields.takeIf { it.isNotBlank() }
                ?.let { "${props.baseUrl}/me?access_token=$token&fields=${props.fields}" }
                ?: "${props.baseUrl}/me?access_token=$token")
            .retrieve()
            .onStatus({ status -> status.isError }) { response ->
                Mono.error(IllegalStateException("Facebook get information failed: ${response.statusCode()}"))
            }
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
}
