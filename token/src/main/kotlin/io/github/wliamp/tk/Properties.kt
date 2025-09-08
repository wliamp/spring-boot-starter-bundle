package io.github.wliamp.tk

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "token")
data class Properties(
    var backend: String = "ENV",
    var envVar: String = "STARTER_TOKEN_PRIVATE_JWKS_JSON",
    var reloadIntervalSeconds: Long = 300,
    var jwksPath: String = "/oauth2/jwks",
    var issuer: String = "",
    var expireSeconds: Long = 3600,
    var defaultClaims: Map<String, Any> = emptyMap()
)
