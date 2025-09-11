package io.github.wliamp.pro.vrf

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class GoogleTest : ITestSetup<OauthProps.GoogleProps, IOauth> {
    override val server = MockWebServer()
    override lateinit var client: WebClient
    override lateinit var props: OauthProps.GoogleProps
    override lateinit var provider: IOauth
    override val mapper = ObjectMapper()

    override fun buildProps() = OauthProps.GoogleProps().apply {
        clientId = "test-client"
        baseUrl = ""
    }

    override fun buildProvider(props: OauthProps.GoogleProps, client: WebClient) =
        IGoogle(props, client)

    @BeforeAll
    fun beforeAll() = server.start()

    @AfterAll
    fun afterAll() = server.shutdown()

    @BeforeEach
    fun setup() = initServerAndClient()

    @Test
    fun `verify returns true when aud matches`() {
        enqueueJson(server, mapOf("aud" to "test-client"))
        StepVerifier.create(provider.verify("dummy-token"))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `verify returns false when aud mismatches`() {
        enqueueJson(server, mapOf("aud" to "other-client"))
        StepVerifier.create(provider.verify("dummy-token"))
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `verify errors when aud missing`() {
        enqueueJson(server, mapOf("sub" to "123456"))
        StepVerifier.create(provider.verify("dummy-token"))
            .expectError(VerifyParseException::class.java)
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
            .expectError(VerifyConfigException::class.java)
            .verify()
    }

    @Test
    fun `verify builds correct google uri`() {
        enqueueJson(server, mapOf("aud" to "test-client"))
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
