package io.github.wliamp.pro.vrf

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

private class ZaloTest : OauthTest<OauthProps.ZaloProps>({ _ ->
    OauthProps.ZaloProps().apply {
        baseUrl = ""
        version = "/v2.0"
        uri = "/me"
        fields = ""
    }
}) {
    override fun buildProvider(props: OauthProps.ZaloProps, client: WebClient): IOauth =
        IZalo(props, client)

    @Test
    fun `verify returns true when id exists`() {
        enqueueJson(mapOf("id" to "123456"))

        StepVerifier.create(provider.verify("dummy-token"))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `verify errors when id missing`() {
        enqueueJson(mapOf("name" to "Alice"))

        StepVerifier.create(provider.verify("dummy-token"))
            .expectError(OauthParseException::class.java)
            .verify()
    }

    @Test
    fun `verify builds correct zalo uri`() {
        enqueueJson(mapOf("id" to "123456"))

        StepVerifier.create(provider.verify("dummy-token"))
            .expectNext(true)
            .verifyComplete()

        val recorded = server.takeRequest()
        assertEquals(
            "${props.version}${props.uri}?access_token=dummy-token",
            recorded.path
        )
    }

    @Test
    fun `getInfo builds correct uri without fields`() {
        enqueueJson(mapOf("id" to "123", "name" to "Alice"))

        StepVerifier.create(provider.getInfo("dummy-token"))
            .expectNextMatches { it["id"] == "123" && it["name"] == "Alice" }
            .verifyComplete()

        val recorded = server.takeRequest()
        assertEquals(
            "${props.version}${props.uri}?access_token=dummy-token",
            recorded.path
        )
    }

    @Test
    fun `getInfo builds correct uri with fields`() {
        val customProps = OauthProps.ZaloProps().apply {
            baseUrl = props.baseUrl
            version = props.version
            uri = props.uri
            fields = "id,name"
        }
        val zalo = IZalo(customProps, client)

        enqueueJson(mapOf("id" to "123", "name" to "Alice"))

        StepVerifier.create(zalo.getInfo("dummy-token"))
            .expectNextMatches { it["id"] == "123" && it["name"] == "Alice" }
            .verifyComplete()

        val recorded = server.takeRequest()
        assertEquals(
            "${props.version}${props.uri}?access_token=dummy-token&fields=id,name",
            recorded.path
        )
    }
}
