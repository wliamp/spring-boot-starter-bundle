package io.github.wliamp.tk

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.RSAKey
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import java.security.KeyPairGenerator
import kotlin.test.assertNotNull

class AutoConfigTest {

    private fun baseContextRunner() = ApplicationContextRunner()
        .withUserConfiguration(AutoConfig::class.java)
        .withInitializer {
            it.beanFactory.registerSingleton("objectMapper", ObjectMapper())
        }

    @BeforeEach
    fun clearEnv() {
        System.clearProperty("STARTER_TOKEN_PRIVATE_JWKS_JSON")
        System.clearProperty("MY_ENV")
    }

    @Test
    fun `should create beans with default env var`() {
        System.setProperty(
            "STARTER_TOKEN_PRIVATE_JWKS_JSON",
            """
            {
              "currentKid": "k1",
              "graceKids": [],
              "keys": [${RSAKeyGenerator.generate()}]
            }
            """.trimIndent()
        )

        baseContextRunner()
            .withPropertyValues(
                "token.backend=ENV",
                "token.env-var=STARTER_TOKEN_PRIVATE_JWKS_JSON"
            )
            .run {
                assertNotNull(it.getBean(Properties::class.java))
                assertNotNull(it.getBean(SecretLoader::class.java))
                assertNotNull(it.getBean(KeySetManager::class.java))
                assertNotNull(it.getBean(JwtEncoder::class.java))
                assertNotNull(it.getBean(ReactiveJwtDecoder::class.java))
                assertNotNull(it.getBean(TokenUtil::class.java))
            }
    }

    @Test
    fun `should create beans with custom env var`() {
        System.setProperty(
            "MY_ENV",
            """
            {
              "currentKid": "k1",
              "graceKids": [],
              "keys": [${RSAKeyGenerator.generate()}]
            }
            """.trimIndent()
        )

        baseContextRunner()
            .withPropertyValues(
                "token.backend=ENV",
                "token.env-var=MY_ENV",
                "token.issuer=http://custom-issuer"
            )
            .run {
                assertNotNull(it.getBean(Properties::class.java))
                assertNotNull(it.getBean(SecretLoader::class.java))
                assertNotNull(it.getBean(KeySetManager::class.java))
                assertNotNull(it.getBean(JwtEncoder::class.java))
                assertNotNull(it.getBean(ReactiveJwtDecoder::class.java))
                assertNotNull(it.getBean(TokenUtil::class.java))
            }
    }
}

object RSAKeyGenerator {
    fun generate(): String {
        val gen = KeyPairGenerator.getInstance("RSA")
        gen.initialize(2048)
        val keyPair = gen.generateKeyPair()

        val rsaKey = RSAKey.Builder(keyPair.public as java.security.interfaces.RSAPublicKey)
            .privateKey(keyPair.private as java.security.interfaces.RSAPrivateKey)
            .keyID("k1")
            .build()

        return rsaKey.toJSONString()
    }
}
