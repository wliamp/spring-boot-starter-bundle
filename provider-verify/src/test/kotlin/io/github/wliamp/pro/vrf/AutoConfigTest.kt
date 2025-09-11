package io.github.wliamp.pro.vrf

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class AutoConfigTest {

    private val baseRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(OauthAutoConfig::class.java))

    @Test
    fun `when all enabled then all beans created`() {
        baseRunner
            .withPropertyValues(
                "provider.oauth.facebook.enabled=true",
                "provider.oauth.google.enabled=true",
                "provider.oauth.zalo.enabled=true"
            )
            .run {
                assertThat(it).hasSingleBean(OauthFacebook::class.java)
                assertThat(it).hasSingleBean(OauthGoogle::class.java)
                assertThat(it).hasSingleBean(OauthZalo::class.java)
                assertThat(it).hasSingleBean(OauthProvider::class.java)
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
            .run {
                assertThat(it).doesNotHaveBean(OauthFacebook::class.java)
                assertThat(it).hasSingleBean(OauthGoogle::class.java)
                assertThat(it).hasSingleBean(OauthZalo::class.java)
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
            .run {
                assertThat(it).hasSingleBean(OauthFacebook::class.java)
                assertThat(it).doesNotHaveBean(OauthGoogle::class.java)
                assertThat(it).hasSingleBean(OauthZalo::class.java)
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
            .run {
                assertThat(it).hasSingleBean(OauthFacebook::class.java)
                assertThat(it).hasSingleBean(OauthGoogle::class.java)
                assertThat(it).doesNotHaveBean(OauthZalo::class.java)
            }
    }
}
