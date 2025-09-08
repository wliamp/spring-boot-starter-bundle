package io.github.wliamp.tk

import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PropsBindingTest {
    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(TestConfig::class.java)

    @EnableConfigurationProperties(Properties::class)
    private class TestConfig

    @Test
    fun `should bind default values when no config`() {
        contextRunner.run { ctx ->
            val props = ctx.getBean(Properties::class.java)
            assertEquals("ENV", props.backend)
            assertEquals("STARTER_TOKEN_PRIVATE_JWKS_JSON", props.envVar)
            assertEquals(300, props.reloadIntervalSeconds)
            assertEquals("/oauth2/jwks", props.jwksPath)
            assertEquals("", props.issuer)
            assertEquals(3600, props.expireSeconds)
            assertTrue(props.defaultClaims.isEmpty())
        }
    }

    @Test
    fun `should override values when config is provided`() {
        contextRunner
            .withPropertyValues(
                "token.backend=FILE",
                "token.env-var=CUSTOM_ENV",
                "token.reload-interval-seconds=120",
                "token.jwks-path=/keys/jwks",
                "token.issuer=http://issuer",
                "token.expire-seconds=999",
                "token.default-claims.role=admin"
            )
            .run { ctx ->
                val props = ctx.getBean(Properties::class.java)
                assertEquals("FILE", props.backend)
                assertEquals("CUSTOM_ENV", props.envVar)
                assertEquals(120, props.reloadIntervalSeconds)
                assertEquals("/keys/jwks", props.jwksPath)
                assertEquals("http://issuer", props.issuer)
                assertEquals(999, props.expireSeconds)
                assertEquals("admin", props.defaultClaims["role"])
            }
    }
}
