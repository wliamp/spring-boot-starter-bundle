package io.github.wliamp.tk

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.Collections
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.map

interface SecretLoader {
    fun loadPrivateJwksJson(): String
}

class EnvSecretLoader(private val props: Properties) : SecretLoader {
    override fun loadPrivateJwksJson(): String =
        System.getenv(props.envVar) ?: error("Env var ${props.envVar} not found")
}

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

@Component
@EnableScheduling
class KeySetManager(
    private val loader: SecretLoader,
    private val props: Properties,
    private val objectMapper: ObjectMapper
) {
    private val cache = AtomicReference<PrivateKeySet>()

    init { reload() }

    @Scheduled(fixedDelayString = "\${starter.token.reload-interval-seconds:300}000")
    fun scheduledReload() = reload()

    fun reload() {
        val json = loader.loadPrivateJwksJson()
        val root = objectMapper.readTree(json)
        val currentKid = root["currentKid"].asText()
        val graceKids = root["graceKids"]?.map { it.asText() }?.toSet() ?: Collections.emptySet()
        val keysNode = root["keys"]
        val privateKeys = keysNode.map { k ->
            val map: Map<String, Any> = objectMapper.convertValue(k, object : TypeReference<Map<String, Any>>() {})
            val jwk = JWK.parse(map)
            jwk as? RSAKey ?: error("Key is not RSA")
        }
        cache.set(PrivateKeySet(currentKid, graceKids, privateKeys))
    }

    fun currentKeySet(): PrivateKeySet = cache.get() ?: error("Keys not loaded")

    fun signingJwkSource(): JWKSource<SecurityContext> = JWKSource { selector, _ ->
        val ks = currentKeySet()
        val jwkSet = JWKSet(ks.privateKeys)
        val matched = selector.select(jwkSet)
        if (matched.isEmpty()) listOf(ks.active()) else matched
    }
}
