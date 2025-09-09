package io.github.wliamp.pro.vrf

import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal class IFacebook internal constructor(
    private val props: Properties.FacebookProps,
    private val webClient: WebClient
) : IOauth {
    private val oauth = Oauth.FACEBOOK

    override fun verify(token: String): Mono<Boolean> =
        props.takeIf {
            it.appId.isNotBlank() &&
                it.accessToken.isNotBlank()
        }?.let { p ->
            fetchPayload("${props.baseUrl}/debug_token?input_token=$token&access_token=${props.accessToken}")
                .map {
                    val data = it["data"] as? Map<*, *>
                        ?: throw OauthParseException(oauth, "Missing 'data' in response")
                    p.appId == (data["app_id"]?.toString()
                        ?: throw OauthParseException(oauth, "Missing 'app_id' in response"))
                }
        } ?: Mono.error(
            OauthConfigException(
                oauth, "Missing 'provider.oauth.facebook.app-id' or 'provider.oauth.facebook.access-token'"
            )
        )

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        fetchPayload(
            props.fields.takeIf { it.isNotBlank() }
                ?.let { "${props.baseUrl}/me?access_token=$token&fields=$it" }
                ?: "${props.baseUrl}/me?access_token=$token"
        )

    private fun fetchPayload(uri: String): Mono<Map<String, Any>> =
        webClient.get()
            .uri(uri)
            .retrieve()
            .onStatus({ it.isError }) { resp ->
                resp.bodyToMono(String::class.java)
                    .flatMap {
                        Mono.error(OauthHttpException(oauth, resp.statusCode().value(), it))
                    }
            }
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .onErrorMap {
                when (it) {
                    is OauthException -> it
                    is java.net.ConnectException,
                    is java.net.SocketTimeoutException,
                    is org.springframework.web.reactive.function.client.WebClientRequestException ->
                        OauthNetworkException(oauth, it)

                    is com.fasterxml.jackson.core.JsonProcessingException ->
                        OauthParseException(oauth, "Invalid JSON", it)

                    is org.springframework.core.codec.DecodingException -> {
                        val cause = it.cause
                        if (cause is com.fasterxml.jackson.core.JsonProcessingException)
                            OauthParseException(oauth, "Invalid JSON", cause)
                        else OauthParseException(oauth, "Invalid JSON", it)
                    }

                    else -> OauthUnexpectedException(oauth, it)
                }
            }

}
