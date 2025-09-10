package io.github.wliamp.pro.vrf

import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal class IGoogle internal constructor(
    private val props: OauthProps.GoogleProps,
    private val webClient: WebClient
) : IOauth {
    private val oauth = Oauth.GOOGLE.name

    override fun verify(token: String): Mono<Boolean> =
        props.takeIf { it.clientId.isNotBlank() }
            ?.let { p ->
                fetchGoogle(token)
                    .map {
                        p.clientId == (it["aud"]?.toString()
                            ?: throw VerifyParseException(oauth, "Missing 'aud' in response"))
                    }
            }
            ?: Mono.error(
                VerifyConfigException(
                    oauth,
                    "Missing " +
                        "'provider.oauth.google.client-id'"
                )
            )

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        fetchGoogle(token)

    private fun fetchGoogle(token: String) =
        webClient.fetchPayload(
            HttpMethod.GET,
            "${props.baseUrl}${props.uri}?id_token=$token",
            oauth
        )
}
