package io.github.wliamp.pro.vrf

import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal class IZalo internal constructor(
    private val props: OauthProps.ZaloProps,
    private val webClient: WebClient
) : IOauth {
    private val oauth = Oauth.ZALO

    private val url = "${props.baseUrl}${props.version}${props.uri}"

    override fun verify(token: String): Mono<Boolean> =
        fetchZalo("${url}?access_token=$token")
            .map {
                it["id"]?.toString()
                    ?: throw OauthParseException(oauth, "Missing 'id' in response")
                true
            }

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        fetchZalo(
            props.fields.takeIf { it.isNotBlank() }
                ?.let { "${url}?access_token=$token&fields=$it" }
                ?: "${url}?access_token=$token"
        )

    private fun fetchZalo(uri: String) =
        webClient.fetchPayload(HttpMethod.GET, uri, oauth)
}
