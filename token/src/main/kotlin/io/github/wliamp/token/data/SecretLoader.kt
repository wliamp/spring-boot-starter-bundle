package io.github.wliamp.token.data

interface SecretLoader {
    fun loadPrivateJwksJson(): String
}
