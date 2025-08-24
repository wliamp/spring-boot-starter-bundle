package io.github.wliamp.token.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "token")
data class TokenProperties(
    var expireSeconds: Long = 3600,
    var defaultClaims: Map<String, Any> = emptyMap()
)
