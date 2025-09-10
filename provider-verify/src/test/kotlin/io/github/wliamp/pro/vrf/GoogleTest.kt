package io.github.wliamp.pro.vrf

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

private class GoogleTest : OauthTest<OauthProps.GoogleProps>({ _ ->
    OauthProps.GoogleProps().apply {
        clientId = "test-client"
        baseUrl = ""
    }
}) {
    override fun buildProvider(props: OauthProps.GoogleProps, client: WebClient): IOauth =
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
        val bad = OauthProps.GoogleProps().apply {
            clientId = ""
            baseUrl = ""
        }
        val g = IGoogle(bad, client)

        StepVerifier.create(g.verify("dummy-token"))
            .expectError(OauthConfigException::class.java)
            .verify()
    }

    @Test
    fun `verify builds correct google uri`() {
        enqueueJson(mapOf("aud" to "test-client"))

        StepVerifier.create(provider.verify("dummy-token"))
            .expectNext(true)
            .verifyComplete()

        val recorded = server.takeRequest()
        assertEquals(
            "${props.uri}?id_token=dummy-token",
            recorded.path
        )
    }
}
