package io.github.wliamp.pro.pay

import io.github.wliamp.pro.pay.config.PaymentProviderAutoConfig
import io.github.wliamp.pro.pay.config.PaymentProviderProps
import io.github.wliamp.pro.pay.impl.AuthorizeNetGtw
import io.github.wliamp.pro.pay.impl.VnPayGtw
import io.github.wliamp.pro.pay.impl.ZaloPayGtw
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class TestClass {
    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(PaymentProviderAutoConfig::class.java))
        .withPropertyValues(
            "provider.payment.authorize-net.enabled=true",
            "provider.payment.vn-pay.enabled=true",
            "provider.payment.zalo-pay.enabled=true",
            "provider.payment.authorize-net.api-login-id=test-login",
            "provider.payment.authorize-net.transaction-key=test-key",
            "provider.payment.vn-pay.secret-key=test-vnpay",
            "provider.payment.vn-pay.tmn-code=tmn123",
            "provider.payment.zalo-pay.app-id=1001",
            "provider.payment.zalo-pay.key1=zalo-secret"
        )

    @Test
    fun `when enabled then all beans should be created`() {
        contextRunner.run { ctx ->
            assertThat(ctx).hasSingleBean(PaymentProviderProps::class.java)
            assertThat(ctx).hasSingleBean(PaymentProvider::class.java)

            // check individual gateway beans
            assertThat(ctx).hasSingleBean(AuthorizeNetGtw::class.java)
            assertThat(ctx).hasSingleBean(VnPayGtw::class.java)
            assertThat(ctx).hasSingleBean(ZaloPayGtw::class.java)
        }
    }

    @Test
    fun `when authorizeNet disabled then bean should not exist`() {
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PaymentProviderAutoConfig::class.java))
            .withPropertyValues(
                "provider.payment.authorize-net.enabled=false",
                "provider.payment.vn-pay.enabled=true",
                "provider.payment.zalo-pay.enabled=true"
            )
            .run { ctx ->
                assertThat(ctx).doesNotHaveBean(AuthorizeNetGtw::class.java)
                assertThat(ctx).hasSingleBean(VnPayGtw::class.java)
                assertThat(ctx).hasSingleBean(ZaloPayGtw::class.java)
            }
    }

    @Test
    fun `props binding should map values correctly`() {
        contextRunner.run { ctx ->
            val props = ctx.getBean(PaymentProviderProps::class.java)

            assertThat(props.authorizeNet.apiLoginId).isEqualTo("test-login")
            assertThat(props.authorizeNet.transactionKey).isEqualTo("test-key")
            assertThat(props.vnPay.secretKey).isEqualTo("test-vnpay")
            assertThat(props.vnPay.tmnCode).isEqualTo("tmn123")
            assertThat(props.zaloPay.appId).isEqualTo(1001)
            assertThat(props.zaloPay.key1).isEqualTo("zalo-secret")
        }
    }

    @Test
    fun `context loads without any config (defaults)`() {
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PaymentProviderAutoConfig::class.java))
            .run { ctx ->
                // all defaults should not break
                assertThat(ctx).hasSingleBean(PaymentProviderProps::class.java)
            }
    }
}
