package io.github.wliamp.tk

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.scheduling.TaskScheduler
import java.time.Duration
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.map

fun interface SecretLoader {
    fun loadPrivateJwksJson(): String
}

class EnvSecretLoader(private val props: Properties) : SecretLoader {
    override fun loadPrivateJwksJson(): String =
        System.getenv(props.envVar)
            ?: System.getProperty(props.envVar)
            ?: error("Env var ${props.envVar} not found")
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

class KeySetManager(
    private val loader: SecretLoader,
    props: Properties,
    private val objectMapper: ObjectMapper,
    taskScheduler: TaskScheduler
) {
    private val cache = AtomicReference<PrivateKeySet>()

    init {
        reload()
        taskScheduler.scheduleAtFixedRate(
            { reload() },
            Duration.ofSeconds(props.reloadIntervalSeconds)
        )
    }

    fun reload() {
        val json = loader.loadPrivateJwksJson()
        val root = objectMapper.readTree(json)
        val currentKid = root["currentKid"].asText()
        val graceKids = root["graceKids"]?.map { it.asText() }?.toSet() ?: emptySet()
        val keysNode = root["keys"]
        val privateKeys = keysNode.map { k ->
            val map: Map<String, Any> =
                objectMapper.convertValue(k, object : TypeReference<Map<String, Any>>() {})
            val jwk = JWK.parse(map)
            jwk as? RSAKey ?: error("Key is not RSA")
        }
        cache.set(PrivateKeySet(currentKid, graceKids, privateKeys))
    }

    fun currentKeySet(): PrivateKeySet =
        cache.get() ?: error("Keys not loaded")

    fun signingJwkSource(): JWKSource<SecurityContext> = JWKSource { selector, _ ->
        val ks = currentKeySet()
        val jwkSet = JWKSet(ks.privateKeys)
        val matched = selector.select(jwkSet)
        if (matched.isEmpty()) listOf(ks.active()) else matched
    }
}

