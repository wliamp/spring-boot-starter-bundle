package io.wliamp.token.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "token")
data class TokenProperties(
    var googleClientId: String = "",
    var googleTokenInfoUrl: String = "",
    var facebookAppId: String = "",
    var facebookTokenInfoUrl: String = "",
    var facebookAppAccessToken: String = "", // app_id|app_secret
    var zaloAppId: String = "",
    var zaloTokenInfoUrl: String = ""
)
