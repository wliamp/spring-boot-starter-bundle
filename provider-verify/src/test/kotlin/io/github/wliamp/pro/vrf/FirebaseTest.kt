package io.github.wliamp.pro.vrf

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class FirebaseTest : ITestSetup<OtpProps.FirebaseProps, IOtp> {
    override val server = MockWebServer()
    override lateinit var client: WebClient
    override lateinit var props: OtpProps.FirebaseProps
    override lateinit var provider: IOtp
    override val mapper = ObjectMapper()

    override fun buildProps() = OtpProps.FirebaseProps(
        baseUrl = "",
        version = "v1",
        uri = "/verify",
        apiKey = "dummy-key"
    )

    override fun buildProvider(props: OtpProps.FirebaseProps, client: WebClient) =
        OtpFirebase(props, client)

    @BeforeAll
    fun beforeAll() = server.start()

    @AfterAll
    fun afterAll() = server.shutdown()

    @BeforeEach
    fun setup() = initServerAndClient()

    @Test
    fun `verify returns true when phoneNumber present`() {
        enqueueJson(server, mapOf("phoneNumber" to "+84900123456"))

        StepVerifier.create(provider.verify("dummy-code"))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `verify errors when phoneNumber missing`() {
        enqueueJson(server, mapOf("sessionInfo" to "123"))

        StepVerifier.create(provider.verify("dummy-code"))
            .expectError(VerifyParseException::class.java)
            .verify()
    }

    @Test
    fun `getInfo returns full payload`() {
        val payload = mapOf("phoneNumber" to "+84900123456", "sessionInfo" to "dummy-code")
        enqueueJson(server, payload)

        StepVerifier.create(provider.getInfo("dummy-code"))
            .expectNextMatches {
                it["phoneNumber"] == "+84900123456" && it["sessionInfo"] == "dummy-code"
            }
            .verifyComplete()
    }

    @Test
    fun `verify builds correct Firebase uri with body`() {
        enqueueJson(server, mapOf("phoneNumber" to "+84900123456"))

        StepVerifier.create(provider.verify("dummy-code"))
            .expectNext(true)
            .verifyComplete()

        val recorded = server.takeRequest()

        assertEquals("/v1/verify", recorded.requestUrl?.encodedPath)
        assertEquals("key=dummy-key", recorded.requestUrl?.encodedQuery)

        assertEquals(
            """{"sessionInfo":"dummy-code","code":"dummy-code"}""",
            recorded.body.readUtf8()
        )
    }
}
