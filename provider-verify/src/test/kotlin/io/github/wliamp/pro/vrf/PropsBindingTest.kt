package io.github.wliamp.pro.vrf

import io.github.wliamp.pro.vrf.config.VerifyProviderAutoConfig
import io.github.wliamp.pro.vrf.config.VerifyProviderProps
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class PropsBindingTest {

    private val baseRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(VerifyProviderAutoConfig::class.java))

    @Test
    fun `properties should bind correctly`() {
        baseRunner
            .withPropertyValues(
                "provider.oauth.facebook.base-url=https://custom.fb",
                "provider.oauth.facebook.app-id=fb-app",
                "provider.oauth.facebook.access-token=fb-token",
                "provider.oauth.facebook.fields=id,name",
                "provider.oauth.google.base-url=https://custom.google",
                "provider.oauth.google.client-id=google-client",
                "provider.oauth.zalo.base-url=https://custom.zalo",
                "provider.oauth.zalo.fields=id,picture"
            )
            .run { ctx ->
                val props = ctx.getBean(VerifyProviderProps::class.java)

                assertThat(props.facebook.baseUrl).isEqualTo("https://custom.fb")
                assertThat(props.facebook.appId).isEqualTo("fb-app")
                assertThat(props.facebook.accessToken).isEqualTo("fb-token")
                assertThat(props.facebook.fields).isEqualTo("id,name")

                assertThat(props.google.baseUrl).isEqualTo("https://custom.google")
                assertThat(props.google.clientId).isEqualTo("google-client")

                assertThat(props.zalo.baseUrl).isEqualTo("https://custom.zalo")
                assertThat(props.zalo.fields).isEqualTo("id,picture")
            }
    }

    @Test
    fun `defaults should apply when no config provided`() {
        baseRunner.run { ctx ->
            val props = ctx.getBean(VerifyProviderProps::class.java)

            assertThat(props.facebook.baseUrl).isEqualTo("https://graph.facebook.com")
            assertThat(props.google.baseUrl).isEqualTo("https://oauth2.googleapis.com/tokeninfo")
            assertThat(props.zalo.baseUrl).isEqualTo("https://graph.zalo.me/v2.0/me")
        }
    }
}
