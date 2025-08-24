package  io.github.wliamp.provider.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "token")
data class ProviderProperties(
    var googleClientId: String = "",
    var googleTokenInfoUrl: String = "",
    var facebookAppId: String = "",
    var facebookTokenInfoUrl: String = "",
    var facebookAppAccessToken: String = "",
    var facebookInfoFields: String = "",
    var zaloAppId: String = "",
    var zaloTokenInfoUrl: String = "",
    var zaloInfoFields: String = "",
    var expireSeconds: Long = 3600,
    var defaultClaims: Map<String, Any> = emptyMap()
)
