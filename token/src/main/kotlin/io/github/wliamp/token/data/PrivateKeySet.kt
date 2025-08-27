package io.github.wliamp.token.data

import com.nimbusds.jose.jwk.RSAKey

data class PrivateKeySet(
    val currentKid: String,
    val graceKids: Set<String>,
    val privateKeys: List<RSAKey>
) {
    fun active(): RSAKey = privateKeys.first { it.keyID == currentKid }
    fun verificationPublicKeys(): List<RSAKey> =
        privateKeys.filter { it.keyID == currentKid || it.keyID in graceKids }
            .map { it.toPublicJWK().toRSAKey() }
}

