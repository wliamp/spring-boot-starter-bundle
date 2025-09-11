package io.github.wliamp.pro.vrf

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

internal class ZaloTest : ITestSetup<OauthProps.ZaloProps, IOauth> {
    override lateinit var server: MockWebServer
    override lateinit var client: WebClient
    override lateinit var props: OauthProps.ZaloProps
    override lateinit var provider: IOauth
    override val mapper = ObjectMapper()

    override fun buildProps() = OauthProps.ZaloProps().apply {
        baseUrl = ""
        version = "/v2"
        uri = "/me"
        fields = ""
    }

    override fun buildProvider(props: OauthProps.ZaloProps, client: WebClient) =
        OauthZalo(props, client)

    @BeforeEach
    fun setup() {
        server = MockWebServer()
        server.start()
        initServerAndClient()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `verify returns true when id exists`() {
        enqueueJson(server, mapOf("id" to "123456"))

        StepVerifier.create(provider.verify("dummy-token"))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `verify errors when id missing`() {
        enqueueJson(server, mapOf("name" to "Alice"))

        StepVerifier.create(provider.verify("dummy-token"))
            .expectError(VerifyParseException::class.java)
            .verify()
    }

    @Test
    fun `verify builds correct zalo uri`() {
        enqueueJson(server, mapOf("id" to "123456"))

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
        enqueueJson(server, mapOf("id" to "123", "name" to "Alice"))

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
            baseUrl = ""
            version = "/v2"
            uri = "/me"
            fields = "id,name"
        }
        val zalo = OauthZalo(customProps, client)

        enqueueJson(server, mapOf("id" to "123", "name" to "Alice"))

        StepVerifier.create(zalo.getInfo("dummy-token"))
            .expectNextMatches { it["id"] == "123" && it["name"] == "Alice" }
            .verifyComplete()

        val recorded = server.takeRequest()
        assertEquals(
            "${customProps.version}${customProps.uri}?access_token=dummy-token&fields=${customProps.fields}",
            recorded.path
        )
    }
}
