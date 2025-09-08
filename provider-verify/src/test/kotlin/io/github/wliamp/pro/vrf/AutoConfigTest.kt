package io.github.wliamp.pro.vrf

import io.github.wliamp.pro.vrf.config.VerifyProviderAutoConfig
import io.github.wliamp.pro.vrf.oauth.FacebookOauth
import io.github.wliamp.pro.vrf.oauth.GoogleOauth
import io.github.wliamp.pro.vrf.oauth.ZaloOauth
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class AutoConfigTest {

    private val baseRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(VerifyProviderAutoConfig::class.java))

    @Test
    fun `when all enabled then all beans created`() {
        baseRunner
            .withPropertyValues(
                "provider.oauth.facebook.enabled=true",
                "provider.oauth.google.enabled=true",
                "provider.oauth.zalo.enabled=true"
            )
            .run { ctx ->
                assertThat(ctx).hasSingleBean(FacebookOauth::class.java)
                assertThat(ctx).hasSingleBean(GoogleOauth::class.java)
                assertThat(ctx).hasSingleBean(ZaloOauth::class.java)
                assertThat(ctx).hasSingleBean(OauthProvider::class.java)
            }
    }

    @Test
    fun `when facebook disabled then bean should not exist`() {
        baseRunner
            .withPropertyValues(
                "provider.oauth.facebook.enabled=false",
                "provider.oauth.google.enabled=true",
                "provider.oauth.zalo.enabled=true"
            )
            .run { ctx ->
                assertThat(ctx).doesNotHaveBean(FacebookOauth::class.java)
                assertThat(ctx).hasSingleBean(GoogleOauth::class.java)
                assertThat(ctx).hasSingleBean(ZaloOauth::class.java)
            }
    }

    @Test
    fun `when google disabled then bean should not exist`() {
        baseRunner
            .withPropertyValues(
                "provider.oauth.facebook.enabled=true",
                "provider.oauth.google.enabled=false",
                "provider.oauth.zalo.enabled=true"
            )
            .run { ctx ->
                assertThat(ctx).hasSingleBean(FacebookOauth::class.java)
                assertThat(ctx).doesNotHaveBean(GoogleOauth::class.java)
                assertThat(ctx).hasSingleBean(ZaloOauth::class.java)
            }
    }

    @Test
    fun `when zalo disabled then bean should not exist`() {
        baseRunner
            .withPropertyValues(
                "provider.oauth.facebook.enabled=true",
                "provider.oauth.google.enabled=true",
                "provider.oauth.zalo.enabled=false"
            )
            .run { ctx ->
                assertThat(ctx).hasSingleBean(FacebookOauth::class.java)
                assertThat(ctx).hasSingleBean(GoogleOauth::class.java)
                assertThat(ctx).doesNotHaveBean(ZaloOauth::class.java)
            }
    }
}
