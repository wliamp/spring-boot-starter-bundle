package io.github.wliamp.pro.vrf

import org.junit.jupiter.api.*
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

private class GoogleTest : OauthTest<Properties.GoogleProps>({ _ ->
    Properties.GoogleProps().apply {
        clientId = "test-client"
        baseUrl = "/tokeninfo"
    }
}) {
    override fun buildProvider(props: Properties.GoogleProps, client: WebClient): IOauth =
        IGoogle(props, client)

    @Test
    fun `verify returns true when aud matches`() {
        enqueueJson(mapOf("aud" to "test-client"))

        StepVerifier.create(provider.verify("dummy-token"))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `verify returns false when aud mismatches`() {
        enqueueJson(mapOf("aud" to "other-client"))

        StepVerifier.create(provider.verify("dummy-token"))
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `verify errors when aud missing`() {
        enqueueJson(mapOf("sub" to "123456"))

        StepVerifier.create(provider.verify("dummy-token"))
            .expectError(OauthParseException::class.java)
            .verify()
    }

    @Test
    fun `verify errors when config missing clientId`() {
        val bad = Properties.GoogleProps().apply {
            clientId = ""         // thiếu
            baseUrl = "/tokeninfo"
        }
        val g = IGoogle(bad, client)

        StepVerifier.create(g.verify("dummy-token"))
            .expectError(OauthConfigException::class.java)
            .verify()
    }
}

