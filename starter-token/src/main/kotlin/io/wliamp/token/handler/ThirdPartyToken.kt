package io.wliamp.token.handler

import io.wliamp.token.config.TokenProperties
import org.springframework.web.reactive.function.client.WebClient

class ThirdPartyToken(
    private val props: TokenProperties,
    private val webClient: WebClient
) {
    fun isGoogle(token: String): Boolean {
        if (props.googleClientId.isBlank() || props.googleTokenInfoUrl.isBlank()) {
            throw IllegalStateException("Google client ID or token info URL not configured")
        }
        val response = webClient.get()
            .uri("${props.googleTokenInfoUrl}?id_token=$token")
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()
        val aud = response?.get("aud")?.toString()
        return aud == props.googleClientId
    }

    fun isFacebook(token: String): Boolean {
        if (props.facebookAppId.isBlank() || props.facebookTokenInfoUrl.isBlank() || props.facebookAppAccessToken.isBlank()) {
            throw IllegalStateException("Facebook config missing")
        }
        val response = webClient.get()
            .uri("${props.facebookTokenInfoUrl}?input_token=$token&access_token=${props.facebookAppAccessToken}")
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()
        val appId = (response?.get("data") as? Map<*, *>)?.get("app_id")?.toString()
        return appId == props.facebookAppId
    }

    fun isZalo(token: String): Boolean {
        if (props.zaloAppId.isBlank() || props.zaloTokenInfoUrl.isBlank()) {
            throw IllegalStateException("Zalo config missing")
        }
        val response = webClient.get()
            .uri("${props.zaloTokenInfoUrl}?access_token=$token")
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()
        val id = response?.get("id")?.toString()
        return !id.isNullOrBlank()
    }
}
