package io.github.wliamp.pro.vrf

import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal class IFacebook internal constructor(
    private val props: OauthProps.FacebookProps,
    private val webClient: WebClient
) : IOauth {
    private val oauth = Oauth.FACEBOOK.name

    override fun verify(token: String): Mono<Boolean> =
        props.takeIf {
            it.appId.isNotBlank() &&
                it.accessToken.isNotBlank()
        }?.let { p ->
            fetchFacebook(
                p.vrfUri,
                mapOf(
                    "input_token" to token,
                    "access_token" to p.accessToken
                )
            ).map {
                val data = it["data"] as? Map<*, *>
                    ?: throw VerifyParseException(oauth, "Missing 'data' in response")
                p.appId == (data["app_id"]?.toString()
                    ?: throw VerifyParseException(oauth, "Missing 'app_id' in response"))
            }
        } ?: Mono.error(
            VerifyConfigException(
                oauth,
                "Missing " +
                    "'provider.oauth.facebook.app-id' " +
                    "or " +
                    "'provider.oauth.facebook.access-token'"
            )
        )

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        fetchFacebook(
            props.infoUri,
            props.fields.takeIf { it.isNotBlank() }
                ?.let {
                    mapOf(
                        "access_token" to token,
                        "fields" to it
                    )
                } ?: mapOf("access_token" to token)
        )

    private fun fetchFacebook(path: String, queryParams: Map<String, String>) =
        webClient.fetchPayload(
            HttpMethod.GET,
            "${props.baseUrl}${path}",
            oauth,
            queryParams = queryParams
        )
}
