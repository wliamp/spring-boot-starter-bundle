package io.github.wliamp.pro.vrf

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.*
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

/**
 * Note: OnErrorTest uses GoogleProps + IGoogle as the default provider
 * only to test the generic onError mechanism (provider-agnostic)
 * */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class OnErrorTest : ITestSetup<OauthProps.GoogleProps, IOauth> {
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
    fun `http error OauthHttpException`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"error":"Bad Request"}""")
        )
        StepVerifier.create(provider.verify("dummy-token"))
            .expectError(VerifyHttpException::class.java)
            .verify()
    }

    @Test
    fun `network error OauthNetworkException`() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        StepVerifier.create(provider.verify("dummy-token"))
            .expectError(VerifyNetworkException::class.java)
            .verify()
    }

    @Test
    fun `invalid JSON OauthParseException`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("not-a-json")
        )
        StepVerifier.create(provider.getInfo("dummy-token"))
            .expectError(VerifyParseException::class.java)
            .verify()
    }

    @Test
    fun `getInfo returns payload`() {
        enqueueJson(server, mapOf("id" to "123", "name" to "William"))
        StepVerifier.create(provider.getInfo("dummy-token"))
            .expectNextMatches { it["id"] == "123" && it["name"] == "William" }
            .verifyComplete()
    }
}
