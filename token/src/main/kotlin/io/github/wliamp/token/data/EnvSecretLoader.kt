package io.github.wliamp.token.data

import io.github.wliamp.token.config.TokenProperties

class EnvSecretLoader(private val props: TokenProperties) : SecretLoader {
    override fun loadPrivateJwksJson(): String =
        System.getenv(props.envVar) ?: error("Env var ${props.envVar} not found")
}

