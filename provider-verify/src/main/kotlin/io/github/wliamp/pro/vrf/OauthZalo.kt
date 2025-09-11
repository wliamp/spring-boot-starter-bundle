package io.github.wliamp.pro.vrf

import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal class OauthZalo internal constructor(
    private val props: OauthProps.ZaloProps,
    private val webClient: WebClient
) : IOauth {
    private val oauth = Oauth.ZALO.name

    override fun verify(token: String): Mono<Boolean> =
        fetchZalo(mapOf("access_token" to token))
            .map {
                it["id"]?.toString()
                    ?: throw VerifyParseException(oauth, "Missing 'id' in response")
                true
            }

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        fetchZalo(
            props.fields.takeIf { it.isNotBlank() }
                ?.let {
                    mapOf(
                        "access_token" to token,
                        "fields" to it
                    )
                } ?: mapOf("access_token" to token)
        )

    private fun fetchZalo(queryParams: Map<String, String>) =
        webClient.fetchPayload(
            HttpMethod.GET,
            "${props.baseUrl}${props.version}${props.uri}",
            oauth,
            queryParams = queryParams
        )
}
