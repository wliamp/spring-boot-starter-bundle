package io.github.wliamp.tk

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import kotlin.test.assertNotNull

class AutoConfigTest {
    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(AutoConfig::class.java)
        .withInitializer {
            it.beanFactory.registerSingleton("objectMapper", ObjectMapper())
        }

    @Test
    fun `should create beans with default props`() {
        contextRunner.run {
            assertNotNull(it.getBean(Properties::class.java))
            assertNotNull(it.getBean(SecretLoader::class.java))
            assertNotNull(it.getBean(JwtEncoder::class.java))
            assertNotNull(it.getBean(ReactiveJwtDecoder::class.java))
            assertNotNull(it.getBean(TokenUtil::class.java))
        }
    }

    @Test
    fun `should create beans with custom props`() {
        contextRunner
            .withPropertyValues(
                "token.backend=ENV",
                "token.env-var=MY_ENV",
                "token.issuer=http://custom-issuer"
            )
            .run {
                assertNotNull(it.getBean(Properties::class.java))
                assertNotNull(it.getBean(SecretLoader::class.java))
                assertNotNull(it.getBean(JwtEncoder::class.java))
                assertNotNull(it.getBean(ReactiveJwtDecoder::class.java))
                assertNotNull(it.getBean(TokenUtil::class.java))
            }
    }
}
