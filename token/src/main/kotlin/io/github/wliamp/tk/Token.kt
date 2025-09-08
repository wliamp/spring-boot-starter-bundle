package io.github.wliamp.tk

import java.time.Instant

data class Token(
    val subject: String,
    val type: Type,
    val issuedAt: Instant,
    val expiration: Instant,
    val claims: Map<String, Any>
)

enum class Type {
    ACCESS,
    REFRESH,
    SERVICE
}
