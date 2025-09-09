package io.github.wliamp.pro.vrf

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.*
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.test.StepVerifier
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GoogleTest {

    private lateinit var server: MockWebServer
    private lateinit var client: WebClient
    private lateinit var props: Properties.GoogleProps
    private lateinit var google: IGoogle
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
        props = Properties.GoogleProps().apply {
            clientId = "test-client"
            baseUrl = server.url("/tokeninfo").toString()
        }

        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 200)
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(1, TimeUnit.SECONDS))
                conn.addHandlerLast(WriteTimeoutHandler(1, TimeUnit.SECONDS))
            }

        client = WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()

        google = IGoogle(props, client)
    }

    private fun enqueueJson(body: Any, code: Int = 200) {
        server.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(body))
                .setSocketPolicy(SocketPolicy.KEEP_OPEN)
        )
    }

    @Test
    fun `verify returns true when aud matches`() {
        enqueueJson(mapOf("aud" to "test-client"))

        StepVerifier.create(google.verify("dummy-token"))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `verify returns false when aud mismatches`() {
        enqueueJson(mapOf("aud" to "other-client"))

        StepVerifier.create(google.verify("dummy-token"))
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `verify throws when aud missing`() {
        enqueueJson(mapOf("sub" to "123456"))

        StepVerifier.create(google.verify("dummy-token"))
            .expectError(GoogleParseException::class.java)
            .verify()
    }

    @Test
    fun `getInfo returns payload`() {
        enqueueJson(mapOf("aud" to "test-client", "email" to "user@example.com"))

        StepVerifier.create(google.getInfo("dummy-token"))
            .expectNextMatches { it["email"] == "user@example.com" }
            .verifyComplete()
    }

    @Test
    fun `verify errors when config missing clientId`() {
        val propsNoClient = Properties.GoogleProps().apply {
            clientId = ""
            baseUrl = server.url("/tokeninfo").toString()
        }
        val googleNoClient = IGoogle(propsNoClient, client)

        StepVerifier.create(googleNoClient.verify("dummy-token"))
            .expectError(GoogleConfigException::class.java)
            .verify()
    }

    @Test
    fun `verify errors on HTTP error`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"error":"Bad Request"}""")
        )

        StepVerifier.create(google.verify("dummy-token"))
            .expectError(GoogleHttpException::class.java)
            .verify()
    }

    @Test
    fun `verify errors on network issue`() {
        server.enqueue(
            MockResponse()
                .setSocketPolicy(SocketPolicy.NO_RESPONSE) // sẽ trigger timeout
        )

        StepVerifier.create(google.verify("dummy-token"))
            .expectError(GoogleNetworkException::class.java)
            .verify()
    }

    @Test
    fun `verify errors on unexpected exception`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("not-a-json-object")
        )

        StepVerifier.create(google.getInfo("dummy-token"))
            .expectError(GoogleUnexpectedException::class.java)
            .verify()
    }
}
