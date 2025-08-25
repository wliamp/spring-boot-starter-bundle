package io.github.wliamp.token.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "token")
data class TokenProperties(
    var backend: String = "ENV",
    var envVar: String = "STARTER_TOKEN_PRIVATE_JWKS_JSON",
    var reloadIntervalSeconds: Long = 300,
    var jwksPath: String = "/oauth2/jwks",
    var expireSeconds: Long = 3600,
    var defaultClaims: Map<String, Any> = emptyMap()
)
