package io.github.wliamp.pro.vrf

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.*
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FirebaseTest {

    private lateinit var server: MockWebServer
    private lateinit var client: WebClient
    private lateinit var props: OtpProps.FirebaseProps
    private lateinit var provider: IFirebase
    private val mapper = ObjectMapper()

    @BeforeAll
    fun beforeAll() {
        server = MockWebServer()
        server.start()
    }

    @AfterAll
    fun afterAll() {
        server.shutdown()
    }

    @BeforeEach
    fun setup() {
        val base = server.url("/").toString().removeSuffix("/")
        client = WebClient.builder().baseUrl(base).build()
        props = OtpProps.FirebaseProps(
            baseUrl = base,
            version = "v1",
            uri = "/verify",
            apiKey = "dummy-key"
        )
        provider = IFirebase(props, client)
    }

    private fun enqueueJson(body: Any, code: Int = 200) {
        server.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(body))
        )
    }

    @Test
    fun `verify returns true when phoneNumber present`() {
        enqueueJson(mapOf("phoneNumber" to "+84900123456"))

        StepVerifier.create(provider.verify("dummy-code"))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `verify errors when phoneNumber missing`() {
        enqueueJson(mapOf("sessionInfo" to "123"))

        StepVerifier.create(provider.verify("dummy-code"))
            .expectError(VerifyParseException::class.java)
            .verify()
    }

    @Test
    fun `getInfo returns full payload`() {
        val payload = mapOf("phoneNumber" to "+84900123456", "sessionInfo" to "dummy-code")
        enqueueJson(payload)

        StepVerifier.create(provider.getInfo("dummy-code"))
            .expectNextMatches { it["phoneNumber"] == "+84900123456" && it["sessionInfo"] == "dummy-code" }
            .verifyComplete()
    }

    @Test
    fun `verify builds correct Firebase uri with body`() {
        enqueueJson(mapOf("phoneNumber" to "+84900123456"))

        StepVerifier.create(provider.verify("dummy-code"))
            .expectNext(true)
            .verifyComplete()

        val recorded = server.takeRequest()

        Assertions.assertEquals("/v1/verify", recorded.requestUrl?.encodedPath)
        Assertions.assertEquals("key=dummy-key", recorded.requestUrl?.encodedQuery)

        Assertions.assertEquals(
            """{"sessionInfo":"dummy-code","code":"dummy-code"}""",
            recorded.body.readUtf8()
        )
    }
}
