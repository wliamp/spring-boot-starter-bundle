package io.github.wliamp.token.data

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import io.github.wliamp.token.config.TokenProperties
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.Collections.emptySet
import java.util.concurrent.atomic.AtomicReference

@Component
@EnableScheduling
class KeySetManager(
    private val loader: SecretLoader,
    private val props: TokenProperties,
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
        val graceKids = root["graceKids"]?.map { it.asText() }?.toSet() ?: emptySet()
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
